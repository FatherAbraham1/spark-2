/**
 *
 */
package com.bosscs.spark.commons.extractor.response;

import com.bosscs.spark.commons.extractor.actions.ActionType;

/**
 * @author Jerry Xiong
 */
public class NextResponse<T> extends Response {

    private static final long serialVersionUID = -2647516898871636731L;

    private T data;

    public NextResponse() {
        super();
    }

    public NextResponse(T data) {
        super(ActionType.NEXT);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
