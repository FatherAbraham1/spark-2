/**
 *
 */
package com.bosscs.spark.commons.extractor.response;

import org.apache.spark.Partition;

import com.bosscs.spark.commons.extractor.actions.ActionType;

/**
 * @author Jerry Xiong
 */
public class GetPartitionsResponse extends Response {

    private static final long serialVersionUID = -7728817078374511478L;

    private Partition[] partitions;

    public GetPartitionsResponse() {
        super();
    }

    public GetPartitionsResponse(Partition[] partitions) {
        super(ActionType.GET_PARTITIONS);
        this.partitions = partitions.clone();
    }

    public Partition[] getPartitions() {
        return partitions;
    }
}
