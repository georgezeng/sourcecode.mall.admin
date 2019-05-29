package com.sourcecode.malls.dto;

import com.sourcecode.malls.dto.base.KeyDTO;

public class ClientIdentityBulkDTO extends KeyDTO<Long> {
	private boolean pass;
	private String reason;

	public boolean isPass() {
		return pass;
	}

	public void setPass(boolean pass) {
		this.pass = pass;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
