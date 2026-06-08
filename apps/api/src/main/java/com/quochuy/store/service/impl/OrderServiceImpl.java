package com.quochuy.store.service.impl;

import com.quochuy.store.dto.OrderDto;
import com.quochuy.store.mapper.CartMapper;
import com.quochuy.store.mapper.OrderItemMapper;
import com.quochuy.store.mapper.OrderMapper;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.model.Cart;
import com.quochuy.store.model.Order;
import com.quochuy.store.model.OrderItem;
import com.quochuy.store.model.User;
import com.quochuy.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private final CartMapper cartMapper;
	private final OrderMapper orderMapper;
	private final OrderItemMapper orderItemMapper;
	private final UserMapper userMapper;
	
	@Override
	@Transactional
	public OrderDto createOrder(UUID userId,
								String email,
								String idempotencyKey) {
		/*
		 * 1. Nếu user đã có PENDING order thì trả về order đó.
		 *    Điều này phù hợp với unique index:
		 *    uk_orders_one_pending_per_user
		 */
		Optional<Order> existingPendingOrder = orderMapper.findPendingByUserId(userId);
		if (existingPendingOrder.isPresent()) {
			return getOrderDetail(existingPendingOrder.get().getOrderId(), userId);
		}
		
		/*
		 * 2. Lock cart ACTIVE của user.
		 *    SELECT ... FOR UPDATE giúp trong cùng một thời điểm
		 *    chỉ một transaction được checkout cart này.
		 */
		Cart activeCart = cartMapper.findActiveCartByUserIdForUpdate(userId)
				.orElseThrow(() -> new IllegalStateException("Active cart not found"));
		
		if (activeCart.getNumItemsInCart() <= 0) {
			throw new IllegalStateException("Cannot create order from empty cart");
		}
		
		/*
		 * 3. Lấy email user nếu orders.email bắt buộc NOT NULL.
		 */
		User user = userMapper.findById(userId)
				.orElseThrow(() -> new IllegalStateException("User not found"));
		
		UUID orderId = UUID.randomUUID();
		
		/*
		 * 4. Insert order trước với total tạm thời = 0.
		 *    Sau khi copy order_items xong thì mới update total chuẩn.
		 */
		Order order = new Order();
		order.setOrderId(orderId);
		order.setUserId(userId);
		order.setSourceCartId(activeCart.getCartId());
		order.setEmail(user.getEmail());
		order.setStatus("PENDING");
		order.setProductsCount(0);
		order.setShipping(activeCart.getShipping());
		order.setTax(0);
		order.setOrderTotal(0);
		order.setIdempotencyKey(idempotencyKey);
		order.setCurrency("VND");
		
		orderMapper.insertOrder(order);
		
		/*
		 * 5. Copy cart_items -> order_items.
		 *    Đây là bước snapshot.
		 *    Sau bước này order không phụ thuộc vào cart_items nữa.
		 */
		int copiedRows = orderItemMapper.copyFromCartItems(orderId, activeCart.getCartId());
		
		if (copiedRows <= 0) {
			throw new IllegalStateException("Cannot create order because cart has no items");
		}
		
		/*
		 * 6. Tính lại total từ order_items.
		 *    Không dùng carts.order_total nữa.
		 */
		orderMapper.recalculateOrderTotalsFromItems(orderId);
		
		/*
		 * 7. Mark cart cũ LOCKED.
		 *    Cart này đã được dùng để tạo order pending.
		 */
		cartMapper.markCartLocked(activeCart.getCartId());
		
		/*
		 * 8. Tạo cart ACTIVE mới cho user.
		 *    Vì unique index mới chỉ cho phép một ACTIVE cart/user,
		 *    bước này OK vì cart cũ đã chuyển sang LOCKED.
		 */
		cartMapper.createEmptyActiveCartForUser(
				UUID.randomUUID(),
				userId,
				activeCart.getShipping(),
				activeCart.getTaxRate()
		);
		
		/*
		 * 9. Return order detail từ orders + order_items.
		 */
		return getOrderDetail(orderId, userId);
	
//		Optional<Order> existingByKey = orderMapper.findByIdempotencyKey(key);
//		if (existingByKey.isPresent()) {
//			return toDto(existingByKey.get());
//		}
//		Cart cart = cartMapper.fetchCartByUser(userId);
//		Order order = orderMapper.createOrGetPendingOrder(
//				cart.getNumItemsInCart(),
//				cart.getOrderTotal(),
//				cart.getTax(),
//				cart.getShipping(),
//				email,
//				userId,
//				cart.getCartId(),
//				key
//		);
//		return toDto(order);
	}
	
	@Override
	public OrderDto getOrderDetail(UUID orderId, UUID userId) {
		Order order = orderMapper.findByOrderIdAndUserId(orderId, userId)
				.orElseThrow(() -> new IllegalStateException("Order not found"));
		
		List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
		
		return OrderDto.from(order, items);
	}
	
	@Override
	public OrderDto exitsOrder(String key) {
		return orderMapper.findByIdempotencyKey(key)
				.map(this::toDto)
				.orElseThrow(
						() -> new IllegalStateException(
								"Order not found for idempotency key"));
	}

	public OrderDto existingPendingOrder(UUID userId) {
		return orderMapper.findPendingByUserId(userId)
				.map(this::toDto)
				.orElseThrow(
						() -> new IllegalStateException(
								"Pending order not found for user"));
	}
	
	@Override
	public List<Order> getPaidOrderByUserId(UUID userId) {
		return orderMapper.findByOrderIdIsPaid(userId);
	}

	private OrderDto toDto(Order order) {
		return new OrderDto(order.getOrderId(),
							order.getCartId());
	}
}
