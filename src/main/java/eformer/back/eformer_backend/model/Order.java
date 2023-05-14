package eformer.back.eformer_backend.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import eformer.back.eformer_backend.model.keys.OrderItemId;
import eformer.back.eformer_backend.repository.ItemRepository;
import eformer.back.eformer_backend.repository.OrderItemsRepository;
import eformer.back.eformer_backend.utility.NegativeQuantityException;


@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private final Integer orderId;

    @Column(name = "total")
    private Double total;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private final Timestamp creationDate;

    @Column(name = "number_of_items")
    private Integer numberOfItems;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "status")
    private String status;

    @ManyToOne
    private User customer;

    @ManyToOne
    private User employee;

    @Column(name = "note")
    private String note;

    @Transient
    @JsonIgnore
    private static OrderItemsRepository orderItemsManager;

    @Transient
    @JsonIgnore
    private static ItemRepository itemsManager;

    protected Order(Integer orderId, Double total, Timestamp creationDate,
                    Integer numberOfItems, Double amountPaid,
                    String status, User customer,
                    User employee, String note) {
        this.orderId = orderId;
        this.creationDate = creationDate;
        this.numberOfItems = numberOfItems;
        setTotal(total);
        setAmountPaid(amountPaid);
        setStatus(status);
        setCustomer(customer);
        setEmployee(employee);
        setNote(note);
    }

    protected Order() {
        this(null, null);
    }

    public Order(User customer, User employee) {
        this(-1, 0.0, new Timestamp(new Date().getTime()), 0,
                0.0, "", null, null, "");
    }

    public Double getTotal() {
        return total;
    }

    private void setTotal(Double total) {
        this.total = total;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate.toLocalDateTime();
    }

    public Integer getNumberOfItems() {
        return numberOfItems;
    }

    public static void setOrderItemsManager(OrderItemsRepository manager) {
        orderItemsManager = manager;
    }

    public static void setItemsManager(ItemRepository manager) {
        itemsManager = manager;
    }

    public void addToOrder(Item item, Integer quantity) {
        editItem(item, quantity);
    }

    public void removeFromOrder(Item item, Integer quantity) {
        editItem(item, -quantity);
    }

    public void editItem(Item item, Integer newQuantity) {
        var orderItem = orderItemsManager.findById(new OrderItemId(item.getItemId(), getOrderId()));

        if (orderItem.isPresent() && (newQuantity > 0 || item.getQuantity() >= -newQuantity)) {
            numberOfItems += newQuantity;
            total += newQuantity * item.getUnitPrice();

            orderItem.get().addQuantity(newQuantity);

            /* Remove if quantity is zero */
            if (orderItem.get().getQuantity() == 0) {
                orderItemsManager.delete(orderItem.get());
            } else {
                orderItemsManager.save(orderItem.get());
            }

            /* If negative returns the items, positive takes them */
            item.addQuantity(-newQuantity);
        } else if (newQuantity > 0 && item.removeQuantity(newQuantity)) {
            numberOfItems += newQuantity;
            total += item.getUnitPrice() * newQuantity;

            var newOrderItem = new OrderItem(this, item, newQuantity);

            orderItemsManager.save(newOrderItem);
        } else {
            throw new NegativeQuantityException();
        }

        itemsManager.save(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(getOrderId(), order.getOrderId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId());
    }
}