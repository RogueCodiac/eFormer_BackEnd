package eformer.back.eformer_backend;

import eformer.back.eformer_backend.model.Order;
import eformer.back.eformer_backend.repository.ItemRepository;
import eformer.back.eformer_backend.repository.OrderItemsRepository;
import eformer.back.eformer_backend.repository.OrderRepository;
import eformer.back.eformer_backend.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    final OrderItemsRepository orderItemsRepo;

    final OrderRepository orderRepo;

    final UserRepository userRepo;

    final ItemRepository itemRepo;

    public Application(OrderItemsRepository orderItemsRepo, OrderRepository orderRepo,
                       UserRepository userRepo, ItemRepository itemRepo) {
        this.orderItemsRepo = orderItemsRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;

        Order.setOrderItemsManager(orderItemsRepo);
        Order.setItemsManager(itemRepo);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
