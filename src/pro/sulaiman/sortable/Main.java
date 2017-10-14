package pro.sulaiman.sortable;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sortable Coding Take-Home Project by Anas H. Sulaiman");
        String productsPathname = "products.txt";
        String listingsPathname = "listings.txt";

        if (args.length > 1) {
            productsPathname = args[0];
            listingsPathname = args[1];
            System.out.println("Using specified input files: products=" + productsPathname + ", listings=" + listingsPathname);
        } else {
            System.out.println("Using default input files: products=" + productsPathname + ", listings=" + listingsPathname);
            System.out.println("You may specify custom paths for products and listings as command line arguments respectively");
        }

        SimpleTimer.toggle("total");

        KeywordLinker keywordLinker = new KeywordLinker();
        keywordLinker.link(productsPathname, listingsPathname);

        SimpleTimer.toggle("total");
        System.out.println("Done. Spent " + SimpleTimer.getDuration("total").toString().replace("PT", ""));
    }
}
