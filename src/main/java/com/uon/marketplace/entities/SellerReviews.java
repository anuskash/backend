package com.uon.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seller_reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerReviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;
}
