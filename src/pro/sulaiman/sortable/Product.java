package pro.sulaiman.sortable;

import org.json.JSONObject;

import java.time.LocalDate;

public class Product {
    static final Product NULL_PRODUCT = new Product();

    String manuf;
    String family;
    String model;
    String name;
    LocalDate announcedDate;

    public Product() {
        this.manuf = "";
        this.family = "";
        this.model = "";
        this.name = "";
        this.announcedDate = null;
    }

    public Product(JSONObject jsonProduct) {
        this.manuf = jsonProduct.has("manufacturer") ? jsonProduct.getString("manufacturer") : "";
        this.family = jsonProduct.has("family") ? jsonProduct.getString("family") : "";
        this.model = jsonProduct.has("model") ? jsonProduct.getString("model") : "";
        this.name = jsonProduct.has("product_name") ? jsonProduct.getString("product_name") : "";
        this.announcedDate = null;
        if (jsonProduct.has("announced-date")) {
            try {
                this.announcedDate = LocalDate.parse(jsonProduct.getString("announced-date"));
            } catch(Exception ignored) {}
        }
    }

    public JSONObject toJson() {
        JSONObject jsonProduct = new JSONObject();
        jsonProduct.put("product_name", this.name);
        jsonProduct.put("manufacturer", this.manuf);
        jsonProduct.put("family", this.family);
        jsonProduct.put("model", this.model);
        jsonProduct.put("announced-date", this.announcedDate);
        return jsonProduct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (manuf != null ? !manuf.equals(product.manuf) : product.manuf != null) return false;
        if (family != null ? !family.equals(product.family) : product.family != null) return false;
        if (model != null ? !model.equals(product.model) : product.model != null) return false;
        if (name != null ? !name.equals(product.name) : product.name != null) return false;
        return announcedDate != null ? announcedDate.equals(product.announcedDate) : product.announcedDate == null;
    }

    @Override
    public int hashCode() {
        int result = manuf != null ? manuf.hashCode() : 0;
        result = 31 * result + (family != null ? family.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (announcedDate != null ? announcedDate.hashCode() : 0);
        return result;
    }
}
