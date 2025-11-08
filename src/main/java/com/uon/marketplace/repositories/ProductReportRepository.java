package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.ProductReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {
    
    // Find reports by product
    List<ProductReport> findByProductId(Long productId);
    
    // Find reports by reporter
    List<ProductReport> findByReporterId(Long reporterId);
    
    // Find reports by status
    List<ProductReport> findByStatus(String status);
    Long countByStatus(String status);

    // Pageable finder by status
    Page<ProductReport> findByStatusOrderByReportDateDesc(String status, Pageable pageable);
    
    // Find pending reports (for admin queue)
    List<ProductReport> findByStatusOrderByReportDateDesc(String status);
    
    // Find reports reviewed by specific admin
    List<ProductReport> findByReviewedBy(Long adminId);
    
    // Count reports for a product
    Long countByProductId(Long productId);
    
    // Count reports by a user (to detect spam reporters)
    Long countByReporterId(Long reporterId);
    
    // Check if user already reported this product
    boolean existsByProductIdAndReporterId(Long productId, Long reporterId);
    
    // Get all reports for admin review
    @Query("SELECT pr FROM ProductReport pr WHERE pr.status = 'pending' ORDER BY pr.reportDate DESC")
    List<ProductReport> findPendingReports();

        // Generic pageable search across reason/details and id filters
        @Query("""
                SELECT pr FROM ProductReport pr
                WHERE (:status IS NULL OR pr.status = :status)
                    AND (
                                :q IS NULL OR LOWER(pr.reportReason) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR :q IS NULL OR LOWER(pr.reportDetails) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR :q IS NULL OR CAST(pr.productId AS string) LIKE CONCAT('%', :q, '%')
                         OR :q IS NULL OR CAST(pr.reporterId AS string) LIKE CONCAT('%', :q, '%')
                    )
                ORDER BY pr.reportDate DESC
        """)
        Page<ProductReport> searchReports(
                        @Param("status") String status,
                        @Param("q") String q,
                        Pageable pageable);
}
