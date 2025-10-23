package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    @Query("SELECT ci FROM CartItem ci where ci.product.id=?1 AND ci.cart.id=?2")
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci where ci.product.id=?1 AND ci.cart.id=?2")
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Modifying
    @Query("Delete FROM CartItem ci where ci.cart.id = ?1")
    void deleteAllByCartId(Long cartId);
}
