package com.mdservice;

import com.mdservice.service.inter.CartService;
import com.mdservice.service.inter.OrderService;
import com.mdservice.utils.UserLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class CartOrderSchemaAndSeedTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;

    @Test
    public void createTablesAndSeedData() {
        createTables();
        seedData();
    }

    private void createTables() {
        jdbcTemplate.execute("""
                create table if not exists `cart_item` (
                  `id` bigint not null auto_increment,
                  `user_id` bigint not null,
                  `goods_id` bigint not null,
                  `sku_id` bigint not null,
                  `quantity` int not null default 1,
                  `checked` tinyint not null default 1,
                  `create_time` datetime not null default current_timestamp,
                  `update_time` datetime default null on update current_timestamp,
                  primary key (`id`),
                  unique key `uk_user_sku` (`user_id`, `sku_id`),
                  key `idx_user_id` (`user_id`),
                  key `idx_goods_id` (`goods_id`),
                  constraint `fk_cart_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update cascade,
                  constraint `fk_cart_goods` foreign key (`goods_id`) references `goods` (`id`) on delete restrict on update cascade,
                  constraint `fk_cart_sku` foreign key (`sku_id`) references `sku` (`sku_id`) on delete restrict on update cascade
                ) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci;
                """);

        jdbcTemplate.execute("""
                create table if not exists `orders` (
                  `id` bigint not null auto_increment,
                  `order_no` varchar(32) not null,
                  `user_id` bigint not null,
                  `status` tinyint not null default 0,
                  `total_amount` decimal(10,2) not null default 0.00,
                  `pay_amount` decimal(10,2) not null default 0.00,
                  `create_time` datetime not null default current_timestamp,
                  `pay_time` datetime default null,
                  `update_time` datetime default null on update current_timestamp,
                  primary key (`id`),
                  unique key `uk_order_no` (`order_no`),
                  key `idx_user_id` (`user_id`),
                  constraint `fk_order_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update cascade
                ) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci;
                """);

        jdbcTemplate.execute("""
                create table if not exists `order_item` (
                  `id` bigint not null auto_increment,
                  `order_id` bigint not null,
                  `goods_id` bigint not null,
                  `sku_id` bigint not null,
                  `title` varchar(255) not null,
                  `pic` varchar(1024) default null,
                  `price` decimal(10,2) not null,
                  `quantity` int not null,
                  `total_amount` decimal(10,2) not null,
                  `create_time` datetime not null default current_timestamp,
                  primary key (`id`),
                  key `idx_order_id` (`order_id`),
                  constraint `fk_order_item_order` foreign key (`order_id`) references `orders` (`id`) on delete restrict on update cascade,
                  constraint `fk_order_item_goods` foreign key (`goods_id`) references `goods` (`id`) on delete restrict on update cascade,
                  constraint `fk_order_item_sku` foreign key (`sku_id`) references `sku` (`sku_id`) on delete restrict on update cascade
                ) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci;
                """);
    }

    private void seedData() {
        List<Long> userIds = jdbcTemplate.queryForList("select id from user order by id asc limit 3", Long.class);
        if (userIds == null || userIds.isEmpty()) {
            log.info("no user data, skip seed");
            return;
        }
        List<Long> skuIds = jdbcTemplate.queryForList("select sku_id from sku order by sku_id asc limit 6", Long.class);
        if (skuIds == null || skuIds.isEmpty()) {
            log.info("no sku data, skip seed");
            return;
        }

        int ordersCreated = 0;
        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            UserLocal.setUser(String.valueOf(userId));
            try {
                int base = (i * 2) % skuIds.size();
                cartService.add(skuIds.get(base), 1);
                cartService.add(skuIds.get((base + 1) % skuIds.size()), 2);
                Object orderId = orderService.createFromCart(null).getData();
                log.info("created order for userId={}, orderId={}", userId, orderId);
                ordersCreated++;
            } finally {
                UserLocal.removeUser();
            }
        }
        log.info("orders created: {}", ordersCreated);
    }
}

