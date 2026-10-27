package com.example.extractor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LineItem(

        @JsonPropertyDescription(
                "Name or detailed description of the product or service"
        )
        String description,

        @JsonPropertyDescription(
                "Product or service code/SKU if present"
        )
        String productCode,

        @JsonPropertyDescription(
                "Quantity purchased"
        )
        BigDecimal quantity,

        @JsonPropertyDescription(
                "Unit of measurement such as piece, kg, hour, etc."
        )
        String unit,

        @JsonPropertyDescription(
                "Price of one unit before taxes and discounts"
        )
        BigDecimal unitPrice,

        @JsonPropertyDescription(
                "Discount applied to this line item"
        )
        BigDecimal discount,

        @JsonPropertyDescription(
                "Tax rate applied to this line item"
        )
        BigDecimal taxRate,

        @JsonPropertyDescription(
                "Tax amount applied to this line item"
        )
        BigDecimal taxAmount,

        @JsonPropertyDescription(
                "Final total for this line item"
        )
        BigDecimal lineTotal
) {
}