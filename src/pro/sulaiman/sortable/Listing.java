package pro.sulaiman.sortable;

import org.json.JSONObject;

public class Listing {
    String manuf;
    String title;
    String currency;
    String price;

    public Listing() {
        this.manuf = "";
        this.title = "";
        this.currency = "";
        this.price = "";
    }

    public Listing(JSONObject jsonListing) {
        this.manuf = jsonListing.has("manufacturer") ? jsonListing.getString("manufacturer") : "";
        this.title = jsonListing.has("title") ? jsonListing.getString("title") : "";
        this.currency = jsonListing.has("currency") ? jsonListing.getString("currency") : "";
        this.price = jsonListing.has("price") ? jsonListing.getString("price") : "";
    }

    public JSONObject toJson() {
        JSONObject jsonListing = new JSONObject();
        jsonListing.put("title", this.title);
        jsonListing.put("manufacturer", this.manuf);
        jsonListing.put("currency", this.currency);
        jsonListing.put("price", this.price);
        return jsonListing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Listing listing = (Listing) o;

        if (price != null ? !price.equals(listing.price) : listing.price != null) return false;
        if (manuf != null ? !manuf.equals(listing.manuf) : listing.manuf != null) return false;
        if (title != null ? !title.equals(listing.title) : listing.title != null) return false;
        return currency != null ? currency.equals(listing.currency) : listing.currency == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = manuf != null ? manuf.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }
}
