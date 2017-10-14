package pro.sulaiman.sortable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IoUtils {
    public static ProductsReader getProductsReader(String pathname) throws IOException {
        return new ProductsReader(pathname);
    }

    public static ListingReader getListingReader(String pathname) throws IOException {
        return new ListingReader(pathname);
    }

    public static void writeJsonLines(List<JSONObject> jsonObjects, String pathname) throws IOException {
        if (jsonObjects.isEmpty()) return;

        Path filePath = Paths.get(pathname);
        BufferedWriter bw = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"));
        for(JSONObject jsonObject : jsonObjects) {
            bw.write(jsonObject.toString());
            bw.newLine();
        }
        bw.close();
    }

    public static void writeResult(Map<Product, List<Listing>> result) {
        List<JSONObject> jsonResult = new ArrayList<>();
        for(Product p : result.keySet()) {
            if (p == Product.NULL_PRODUCT) continue;
            JSONArray jsonArray = new JSONArray();
            for(Listing l : result.get(p)) {
                jsonArray.put(l.toJson());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_name", p.name);
            jsonObject.put("listings", jsonArray);
            jsonResult.add(jsonObject);
        }
        try {
            writeJsonLines(jsonResult, "results.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static abstract class ItemsReader<T> implements Closeable, Iterator<T> {
        private Scanner scanner;
        private T currentItem;
        private boolean didCallHasNext;
        private boolean didCallNext;

        protected ItemsReader(String pathname) throws IOException {
            scanner = null;
            currentItem = null;
            didCallHasNext = false;
            didCallNext = true;

            Path filepath = Paths.get(pathname);
            if (!Files.isReadable(filepath)) {
                throw new  IOException("cannot read specified file [" + filepath + "]");
            }

            scanner = new Scanner(filepath);
        }

        private void parseNext() throws IllegalStateException {
            currentItem = null;

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try {
                    JSONObject jsonItem = new JSONObject(line);
                    currentItem = createItem(jsonItem);
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        protected abstract T createItem(JSONObject jsonItem);

        @Override
        public void close() throws IOException {
            if (scanner != null) scanner.close();
        }

        @Override
        public boolean hasNext() throws IllegalStateException {
            if (didCallNext) {
                parseNext();
            }
            didCallHasNext = true;
            return currentItem != null;
        }

        @Override
        public T next() throws IllegalStateException {
            if (!didCallHasNext) {
                hasNext();
            }
            didCallNext = true;
            didCallHasNext = false;
            return currentItem;
        }
    }

    public static final class ProductsReader extends ItemsReader<Product> {
        private ProductsReader(String pathname) throws IOException {
            super(pathname);
        }

        @Override
        protected Product createItem(JSONObject jsonItem) {
            return new Product(jsonItem);
        }
    }

    public static final class ListingReader extends ItemsReader<Listing> {
        private ListingReader(String pathname) throws IOException {
            super(pathname);
        }

        @Override
        protected Listing createItem(JSONObject jsonItem) {
            return new Listing(jsonItem);
        }
    }
}
