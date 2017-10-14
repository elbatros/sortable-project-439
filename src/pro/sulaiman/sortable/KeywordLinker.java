package pro.sulaiman.sortable;

import java.io.IOException;
import java.util.*;

public class KeywordLinker {
    /**
     multi-level index of products.
     first level index uses manufacturer attribute.
     second level index uses family attribute.
     e.g.: first, lookup by manufacturer, then lookup by family to get list of products.
     */
    private TreeMap<String, Map<String, List<Product>>> productDict;
    private Map<Product, List<Listing>> productListingsDict;

    public KeywordLinker() {
        productDict = new TreeMap<>(Comparator.naturalOrder());
        productListingsDict = new HashMap<>();
    }

    public void link(String productsPathname, String listingsPathname) {
        // read all products and index them
        try (IoUtils.ProductsReader productsReader = IoUtils.getProductsReader(productsPathname)) {
            while(productsReader.hasNext()) {
                Product p = productsReader.next();

                productDict.computeIfAbsent(clean(p.manuf), k -> new HashMap<>())
                        .computeIfAbsent(clean(p.family), k -> new ArrayList<>())
                        .add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read listings and link
        try (IoUtils.ListingReader listingReader = IoUtils.getListingReader(listingsPathname)) {
            while(listingReader.hasNext()) {
                Listing l = listingReader.next();
                String lTitle = clean(l.title);
                String lManuf = clean(l.manuf);

                // binary search by manufacturer
                SortedMap headMap = productDict.headMap(lManuf, true);
                String matchedManuf = "";
                if (!headMap.isEmpty()) {
                    matchedManuf = headMap.lastKey().toString();
                }

                // make sure we found a match
                if (headMap.isEmpty() || !matchedManuf.regionMatches(0, lManuf, 0, 3)) {
                    // no match
                    productListingsDict.computeIfAbsent(Product.NULL_PRODUCT, k -> new ArrayList<>()).add(l);
                    continue;
                }

                // search family
                SortedMap<Double, List<Product>> possibleProductMatches = new TreeMap<>(Comparator.reverseOrder());
                for(String kwFamily : productDict.get(matchedManuf).keySet()) {
                    Double score = progressiveMatch(lTitle, kwFamily);
                    possibleProductMatches
                            .computeIfAbsent(score, k -> new ArrayList<>())
                            .addAll(productDict.get(matchedManuf).get(kwFamily));
                    // cannot break on perfect match, because there might be multiple perfect matches
                }

                // search by model, starting with best family match
                Product matchedProduct = Product.NULL_PRODUCT;
                for(List<Product> products : possibleProductMatches.values()) {
                    if (matchedProduct != Product.NULL_PRODUCT) break;
                    for(Product p : products) {
                        Double score = progressiveMatch(lTitle, clean(p.model));
                        if (score >= 0.9) {
                            // perfect match
                            matchedProduct = p;
                            break;
                        }
                    }
                }

                productListingsDict.computeIfAbsent(matchedProduct, k -> new ArrayList<>()).add(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        IoUtils.writeResult(productListingsDict);
    }

    /**
    simple text cleaning by removing white spaces from both ends and converting characters to uppercase.
     */
    private String clean(String text) {
        return text.trim().toUpperCase();
    }

    // slightly faster split using StringTokenizer
    /*private List<String> split(String text, String... delimiters) {
        List<String> input = new ArrayList<>();
        List<String> output = new ArrayList<>();

        output.add(text);

        for(String delim : delimiters) {
            input.clear();
            input.addAll(output);
            output.clear();
            for(String in : input) {
                StringTokenizer tokenizer = new StringTokenizer(in, delim, false);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (!"".equals(token)) output.add(token);
                }
            }
        }

        return output;
    }*/

    // a bit more slightly faster split using indexOf
    /*private List<String> split(String text, String... delimiters) {
        int idx, lastIdx;
        List<String> input = new ArrayList<>();
        List<String> output = new ArrayList<>();

        input.add(text);
        for(String delim : delimiters) {
            output.clear();
            for(String in : input) {
                lastIdx = 0;
                while ((idx = in.indexOf(delim, lastIdx)) >= 0) {
                    output.add(in.substring(lastIdx, idx));
                    lastIdx = idx + delim.length();
                }
                output.add(in.substring(lastIdx));
            }
            input.clear();
            input.addAll(output);
        }

        return output;
    }*/

    // much more faster split using indexOf with minimum checks
    private List<String> split(String text, String... delimiters) {
        List<Integer> cutPoints = new ArrayList<>();
        cutPoints.add(0);
        cutPoints.add(text.length());

        for(String delim : delimiters) {
            int lastIdx = 0;
            int idx;
            while((idx = text.indexOf(delim, lastIdx)) >= 0) {
                cutPoints.add(idx);
                lastIdx = idx + delim.length();
                cutPoints.add(lastIdx);
            }
        }

        cutPoints.sort(Comparator.naturalOrder());

        List<String> output = new ArrayList<>(cutPoints.size() + 1);
        for(int i = 0; i < cutPoints.size(); i += 2) {
            output.add(text.substring(cutPoints.get(i), cutPoints.get(i+1)));
        }

        return output;
    }

    /**
     * performs case sensitive matching and returns a score describing the match.
     * the needle is split by [\\s_-] into multiple keywords, which are searched for in order.
     * this method handles the case where the first keyword might appear multiple times, with other keywords appearing later.
     * @param haystack string to search within
     * @param needle string to search for. if needle is the empty string, immediately returns 0
     * @return a score of match. 1 = perfect match, 0 = no match, ]0,1[ = possible match
     */
    private Double progressiveMatch(String haystack, String needle) {
        if ("".equals(needle)) return 0d;

        List<String> keywords = split(needle, " ", "\t", "-", "_");
        String firstKeyword = keywords.get(0);
        int i = haystack.indexOf(firstKeyword);
        Double maxScore = 0d;
        while(i >= 0 && maxScore < 0.9) {
            Double score = progressiveMatch(haystack, keywords, i);
            if (score > maxScore) maxScore = score;
            i = haystack.indexOf(firstKeyword, i+firstKeyword.length());
        }

        return maxScore;
    }

    private Double progressiveMatch(String haystack, List<String> keywords, int offset) {
        Double countMatch = 0d;
        Double countTotal = 0d;
        int lastKeywordEndIndex = offset;

        for(int i = 0; i < keywords.size(); i++) {
            String kw = keywords.get(i);
            if ("".equals(kw)) continue; // avoid useless matches

            countTotal += 1;

            int j = haystack.indexOf(kw, lastKeywordEndIndex);
            if (j < 0) {
                // keyword not found, end
                break;
            }

            char previousChar = j > 0 ? haystack.charAt(j-1) : Character.MIN_VALUE;
            char nextChar = j+kw.length() < haystack.length() - 1 ? haystack.charAt(j+kw.length()) : Character.MIN_VALUE;

            if (i == 0 && !Character.isWhitespace(previousChar) && j != 0) {
                // first keyword & previous character is not a boundary
                break;
            }
            if (i > 0 && j - lastKeywordEndIndex > 1) {
                // middle keyword & does not directly follow previous keyword
                break;
            }
            if (i == keywords.size() -1 && !Character.isWhitespace(nextChar) && j+kw.length()!=haystack.length()) {
                // last keyword & next character is not a boundary
                break;
            }

            countMatch += 1;
            lastKeywordEndIndex = j + kw.length();
        }

        if (Double.compare(countTotal, 0d) == 0) return 0d;

        return countMatch / countTotal;
    }

    // too slow
    /*private Double progressiveRegexMatch(String haystack, String needle) {
        Double score = 0d;
        if ("".equals(needle)) return score;

        String[] keywords = needle.split("[\\s_-]");
        keywords[0] = "(^|\\s)" + keywords[0];
        keywords[keywords.length -1] = keywords[keywords.length -1] + "($|\\s)";
        Pattern regex = Pattern.compile(String.join("[\\s_-]?", keywords));

        if (regex.matcher(haystack).find()) score = 1d;

        return score;
    }*/
}
