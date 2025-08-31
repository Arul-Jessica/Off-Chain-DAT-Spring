// In OrderRepository.java
package com.tokenization.repository;

// ... imports
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // This is a JPQL (Java Persistence Query Language) query.
    // It's like SQL but uses your Java Entity names and fields.
    @Query("SELECT o FROM Order o WHERE o.assetId = :assetId AND o.orderType = :orderType AND o.status = 'OPEN' ORDER BY o.price DESC, o.createdAt ASC")
    List<Order> findOpenBuyOrders(@Param("assetId") Long assetId, @Param("orderType") String orderType);

    @Query("SELECT o FROM Order o WHERE o.assetId = :assetId AND o.orderType = :orderType AND o.status = 'OPEN' ORDER BY o.price ASC, o.createdAt ASC")
    List<Order> findOpenSellOrders(@Param("assetId") Long assetId, @Param("orderType") String orderType);
}