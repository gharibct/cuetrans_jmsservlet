package com.bct.cuetrans;

public class TemplateDef {

	String reportname = null;
	String group = null;
	String subtotal = null;
	String total = null;
	boolean filters = false;
	boolean table = false;
	boolean Chart = false;
	boolean customerlogo = false;
	boolean productlogo = false;
	boolean generatedtime = false;
	String generatedtimelocation = null;
	boolean footer = false;
	boolean pageno = false;  

	public String getReportname() {
		return reportname;
	}

	public void setReportname(String reportname) {
		this.reportname = reportname;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(String subtotal) {
		this.subtotal = subtotal;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public boolean isFilters() {
		return filters;
	}

	public void setFilters(boolean filters) {
		this.filters = filters;
	}

	public boolean isTable() {
		return table;
	}

	public void setTable(boolean table) {
		this.table = table;
	}

	public boolean isChart() {
		return Chart;
	}

	public void setChart(boolean chart) {
		Chart = chart;
	}

	public boolean isCustomerlogo() {
		return customerlogo;
	}

	public void setCustomerlogo(boolean customerlogo) {
		this.customerlogo = customerlogo;
	}

	public boolean isProductlogo() {
		return productlogo;
	}

	public void setProductlogo(boolean productlogo) {
		this.productlogo = productlogo;
	}

	public boolean isGeneratedtime() {
		return generatedtime;
	}

	public void setGeneratedtime(boolean generatedtime) {
		this.generatedtime = generatedtime;
	}

	public String getGeneratedtimelocation() {
		return generatedtimelocation;
	}

	public void setGeneratedtimelocation(String generatedtimelocation) {
		this.generatedtimelocation = generatedtimelocation;
	}

	public boolean isFooter() {
		return footer;
	}

	public void setFooter(boolean footer) {
		this.footer = footer;
	}

	public boolean isPageno() {
		return pageno;
	}

	public void setPageno(boolean pageno) {
		this.pageno = pageno;
	}

}
