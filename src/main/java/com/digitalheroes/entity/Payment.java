package com.digitalheroes.entity;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="payments") @Getter @Setter @NoArgsConstructor
public class Payment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="subscription_id") Subscription subscription;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
 @Column(nullable=false,precision=10,scale=2) BigDecimal amount;
 @Column(nullable=false,length=3) String currency="INR";
 @Enumerated(EnumType.STRING) @Column(nullable=false) PaymentStatus status;
 @Column(nullable=false) String provider="STRIPE";
 @Column(name="provider_transaction_id",unique=true) String providerTransactionId;
 @Column(name="created_at",nullable=false) Instant createdAt=Instant.now();
}
