/**
 *
 */
package com.bosscs.spark.commons.extractor.actions;

/**
 * @author Jerry Xiong
 */
public class HasNextAction<T> extends Action {

    private static final long serialVersionUID = -1270097974102584045L;

    public HasNextAction() {
        super(ActionType.HAS_NEXT);
    }
}
