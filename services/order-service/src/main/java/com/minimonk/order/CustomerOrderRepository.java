package com.minimonk.order;

import com.minimonk.order.api.OrderOverviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    @Query("""
            select new com.minimonk.order.api.OrderOverviewDto(
                o.id, o.customerId, o.status, o.totalAmount, count(i.id), o.createdAt, o.updatedAt
            )
            from CustomerOrder o
            left join o.items i
            group by o.id, o.customerId, o.status, o.totalAmount, o.createdAt, o.updatedAt
            order by o.createdAt desc
            """)
    List<OrderOverviewDto> findOverview();

    @Query("""
            select new com.minimonk.order.api.OrderOverviewDto(
                o.id, o.customerId, o.status, o.totalAmount, count(i.id), o.createdAt, o.updatedAt
            )
            from CustomerOrder o
            left join o.items i
            where o.customerId = :customerId
            group by o.id, o.customerId, o.status, o.totalAmount, o.createdAt, o.updatedAt
            order by o.createdAt desc
            """)
    List<OrderOverviewDto> findOverviewByCustomerId(UUID customerId);

    Optional<CustomerOrder> findById(UUID id);
}
