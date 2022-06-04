package at.kolleg.erplite;

import at.kolleg.erplite.customermanagement.domain.Address;
import at.kolleg.erplite.customermanagement.domain.Customer;
import at.kolleg.erplite.customermanagement.domain.CustomerID;
import at.kolleg.erplite.customermanagement.ports.out.CustomerRepository;
import at.kolleg.erplite.ordermanagement.domain.CustomerData;
import at.kolleg.erplite.ordermanagement.domain.LineItem;
import at.kolleg.erplite.ordermanagement.domain.Order;
import at.kolleg.erplite.ordermanagement.domain.valueobjects.*;
import at.kolleg.erplite.ordermanagement.ports.out.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class ErpliteApplication implements ApplicationRunner, ApplicationListener<ContextRefreshedEvent> {

    public static void main(String[] args) {
        SpringApplication.run(ErpliteApplication.class, args);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Insert a Order for Testing");

        LineItem lineItem1 = new LineItem(new OrderPosition(1), new ProductNumber("P123456789"), new Name("Product 1 Name"), new MonetaryAmount(new BigDecimal(120.98)), new Percentage(20), new Amount(1));
        LineItem lineItem2 = new LineItem(new OrderPosition(2), new ProductNumber("P12345AAgg"), new Name("Product 2 Name"), new MonetaryAmount(new BigDecimal(12.98)), new Percentage(10), new Amount(4));
        LineItem lineItem3 = new LineItem(new OrderPosition(3), new ProductNumber("DP123BBGGG"), new Name("Product 3 Name"), new MonetaryAmount(new BigDecimal(1340)), new Percentage(20), new Amount(1));
        List<LineItem> list = new ArrayList<>();
        list.add(lineItem1);
        list.add(lineItem2);
        list.add(lineItem3);

        Order o = new Order(new OrderID("O323567890"), new CustomerData(new CustomerID("ASDFGJ1234"), new Name("Firstname"), new Name("LastName"), new Email("ab.a@c.de"), "Street", "2345", "Ort", "AT"), LocalDateTime.now(), list, OrderState.PLACED);
        orderRepository.insert(o);

        Optional<Order> o2 = orderRepository.getById(o.getOrderID());

        Order o22 = o2.get();
        o22.orderStateTransitionTo(OrderState.PAYMENT_VERIFIED);
        orderRepository.updateOrderWithNewState(o22);

        System.out.println("Inserting Customer to Test");
        List<Address> addressList = new ArrayList<>();
        addressList.add(new Address("Prausgasse 12", "7543", "Telfs", "Österreich"));
        addressList.add(new Address("Prell 12", "3311", "Telfs", "Österreich"));
        addressList.add(new Address("Hocker 12", "1234", "Innsbruck", "Österreich"));

        Customer customer = new Customer(new CustomerID("CXU1234567"), new Name("Meinhard"), new Name("Franke"), new Email("mein.franke@gmxx.at"), addressList);
        this.customerRepository.insert(customer);

        //TODO: Outbox-Service implementieren, Details siehe: https://medium.com/batc/outbox-polling-reliable-message-broker-publisher-61f46ae65cdd
        //TODO: Outbox im Detail: https://pkritiotis.io/outbox-pattern-implementation-challenges/
        //TODO: Empfänger müssen idempotent sein
        //TODO: Commands, Queries, Responses anstelle von DTOs verwenden ....

    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(100);
        executor.initialize();
        return executor;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping
                .getHandlerMethods();
        map.forEach((key, value) -> System.out.println("Endpoint info: " + key + " " + value));

    }
}
