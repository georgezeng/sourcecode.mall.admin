package com.sourcecode.malls.dto;

import com.sourcecode.malls.dto.base.KeyDTO;

public class ClientIdentityBulkDTO {
	private KeyDTO<Long> ids;
	private boolean pass;
	private String reason;

	public KeyDTO<Long> getIds() {
		return ids;
	}

	public void setIds(KeyDTO<Long> ids) {
		this.ids = ids;
	}

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
