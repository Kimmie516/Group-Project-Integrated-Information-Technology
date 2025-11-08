package sit.int221.backend.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import sit.int221.backend.entities.SaleItem;

import java.util.List;

public class SaleItemSpecifications {

    public static Specification<SaleItem> brandIn(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) {
                return cb.conjunction();
            }
            return cb.lower(root.get("brand").get("name"))
                    .in(brands.stream().map(String::toLowerCase).toList());
        };
    }


    public static Specification<SaleItem> priceBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            } else if (min != null && max != null) {
                if (min > max) {
                    return cb.disjunction();
                }
                return cb.between(root.get("price"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("price"), max);
            }
        };
    }

    public static Specification<SaleItem> storageIn(List<Integer> storageSizes) {
        return (root, query, cb) -> {
            if (storageSizes == null || storageSizes.isEmpty()) {
                return cb.conjunction();
            }

            boolean includeNull = storageSizes.stream().anyMatch(s -> s != null && s < 0);
            List<Integer> normals = storageSizes.stream()
                    .filter(s -> s != null && s >= 0)
                    .toList();

            java.util.List<Predicate> ors = new java.util.ArrayList<>();
            if (!normals.isEmpty()) {
                ors.add(root.get("storageGb").in(normals));
            }
            if (includeNull) {
                ors.add(cb.isNull(root.get("storageGb")));
            }

            return ors.isEmpty() ? cb.conjunction() : cb.or(ors.toArray(new Predicate[0]));
        };
    }

    public static Specification<SaleItem> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String lower = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("description")), lower),
                    cb.like(cb.lower(root.get("model")), lower),
                    cb.like(cb.lower(root.get("color")), lower)
            );
        };
    }
}
