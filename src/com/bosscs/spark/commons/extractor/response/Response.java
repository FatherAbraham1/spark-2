/**
 *
 */
package com.bosscs.spark.commons.extractor.response;

import java.io.Serializable;

import com.bosscs.spark.commons.extractor.actions.ActionType;

/**
 * @author Jerry Xiong
 */
public abstract class Response implements Serializable {

    private static final long serialVersionUID = -3525560371269242119L;

    protected ActionType type;

    protected Response() {
        super();
    }

    public Response(ActionType type) {
        super();
        this.type = type;
    }

    public ActionType getType() {
        return type;
    }
}
