package cn.gmlee.tools.prevent.exception;

import lombok.Getter;

/**
 * 限流异常
 *
 * @author James
 * @since 2023-07-28
 */
@Getter
public class LimitException extends RuntimeException {
	private static final long serialVersionUID = 113112506236589805L;

	protected String code;

	public LimitException() {
		super();
	}

	public LimitException(String code) {
		super();
		this.code = code;
	}

	public LimitException(String code, String message) {
		super(message);
		this.code = code;
	}

	public LimitException(String code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

}
