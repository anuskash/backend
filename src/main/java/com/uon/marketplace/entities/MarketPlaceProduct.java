package com.uon.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketPlaceProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long productId;

	@Column(name = "seller_id", nullable = false)
	private Long sellerId;

    @Column(name ="seller_name", nullable = false)
    private String sellerName;
    @Column(name ="buyer_id", nullable = true)

    private Long buyerId;
    @Column(name ="buyer_name", nullable = true)
    private String buyerName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name="condition", nullable = false)
    private String condition;

	@Column(name = "product_description", length = 255)
	private String productDescription;

	@Column(name = "product_image_url")
	private String productImageUrl;

	@Column(name = "price", nullable = false)
	private BigDecimal price;
    
	@Column(name = "posted_date", nullable = false)
	private LocalDateTime postedDate;

	@Column(name = "last_update")
	private LocalDateTime lastUpdate;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "flagged")
	private Boolean flagged = false;

	@Column(name = "flag_reason", length = 200)
	private String flagReason;

	@Column(name = "report_count")
	private Integer reportCount = 0; // May be null for legacy rows; safeguard in getter

	// ---- Null-safe accessors / helpers ----
	public Integer getReportCount() {
		return reportCount == null ? 0 : reportCount;
	}

	public void setReportCount(Integer reportCount) {
		this.reportCount = reportCount;
	}

	public void incrementReportCount() {
		this.reportCount = getReportCount() + 1;
	}
}

