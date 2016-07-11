/**
 *
 */
package com.bosscs.spark.commons.extractor.actions;

import org.apache.spark.Partition;

import com.bosscs.spark.commons.config.ExtractorConfig;

/**
 * @author Jerry Xiong
 */
public class InitIteratorAction<T> extends Action {

    private static final long serialVersionUID = -1270097974102584045L;

    private ExtractorConfig<T> config;

    private Partition partition;

    public InitIteratorAction() {
        super();
    }

    public InitIteratorAction(Partition partition, ExtractorConfig<T> config) {
        super(ActionType.INIT_ITERATOR);
        this.config = config;
        this.partition = partition;
    }

    public ExtractorConfig<T> getConfig() {
        return config;
    }

    public Partition getPartition() {
        return partition;
    }
}
