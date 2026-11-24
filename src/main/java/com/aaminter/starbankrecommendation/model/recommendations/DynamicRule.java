package com.aaminter.starbankrecommendation.model.recommendations;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * JPA entity representing a persisted dynamic recommendation rule.
 * The rule conditions are stored as JSONB in the recommendation database.
 */
@Entity
@Table(name = "dynamic_rules")
public class DynamicRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_text", nullable = false)
    private String productText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule", columnDefinition = "jsonb", nullable = false)
    private String rule;

    /**
     * Required by JPA for entity instantiation.
     */
    public DynamicRule() {
    }

    /**
     * Creates a dynamic rule entity with its product data and serialized conditions.
     *
     * @param productId product UUID
     * @param productName product display name
     * @param productText recommendation text
     * @param rule serialized dynamic-rule conditions
     */
    public DynamicRule(UUID productId, String productName, String productText, String rule) {
        this.productId = productId;
        this.productName = productName;
        this.productText = productText;
        this.rule = rule;
    }

    /**
     * Returns the database identifier of this dynamic rule.
     *
     * @return dynamic rule identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database identifier of this dynamic rule.
     *
     * @param id dynamic rule identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the UUID of the recommended product.
     *
     * @return product UUID
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * Sets the UUID of the recommended product.
     *
     * @param productId product UUID
     */
    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    /**
     * Returns the display name of the recommended product.
     *
     * @return product display name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the display name of the recommended product.
     *
     * @param productName product display name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the recommendation text associated with the product.
     *
     * @return recommendation text
     */
    public String getProductText() {
        return productText;
    }

    /**
     * Sets the recommendation text associated with the product.
     *
     * @param productText recommendation text
     */
    public void setProductText(String productText) {
        this.productText = productText;
    }

    /**
     * Returns the serialized JSON conditions of this rule.
     *
     * @return dynamic-rule conditions as JSON
     */
    public String getRule() {
        return rule;
    }

    /**
     * Sets the serialized JSON conditions of this rule.
     *
     * @param rule dynamic-rule conditions as JSON
     */
    public void setRule(String rule) {
        this.rule = rule;
    }
}