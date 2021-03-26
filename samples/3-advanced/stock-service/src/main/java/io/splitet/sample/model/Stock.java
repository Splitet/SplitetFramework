package io.splitet.sample.model;

import io.splitet.core.spring.model.JpaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@javax.persistence.Entity(name = "STOCK")
public class Stock extends JpaEntity {

    private String stockName;

    private long remainingStock;

    @Enumerated(EnumType.STRING)
    private StockState state;

}
