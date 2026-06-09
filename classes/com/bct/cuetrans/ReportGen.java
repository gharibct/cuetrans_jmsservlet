package com.bct.cuetrans; 

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;

import com.bct.cuetrans.TemplateDef;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.constant.JasperProperty;
import net.sf.dynamicreports.report.builder.chart.Bar3DChartBuilder;
import net.sf.dynamicreports.report.builder.column.PercentageColumnBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.json.JSONObject;

public class ReportGen extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("ReportGen1-->"+getServletContext().getRealPath("/"));
		
		// Read request body
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append(System.lineSeparator());
		}
		String requestData = buffer.toString();

		// requestData parsing
		JsonElement jelement = new JsonParser().parse(requestData);
		JsonObject jobject = jelement.getAsJsonObject();
		String templateName = jobject.get("templateName").getAsString();
		String exportType = jobject.get("exportType").getAsString();

		JsonArray reportColumns = jobject.get("reportColumn").getAsJsonArray();
		JsonArray reportDatas = jobject.get("reportData").getAsJsonArray();
		JsonObject reportFilters = new JsonObject();
		if(jobject.get("reportFilters")!=null)
			reportFilters = jobject.get("reportFilters").getAsJsonObject();

		ServletOutputStream os = response.getOutputStream();
		try {
			String imgPath = getServletContext().getRealPath("/") + "images/";

			if (exportType != null) {
 
				// Report template fetch
			    final Gson gson = new Gson();
			    TemplateDef templateDef = new TemplateDef(); 
			    HashMap map = new HashMap();
			    
			    
			    ReportTemplates repTemplate = new Gson().fromJson((Reader)new FileReader(getServletContext().getRealPath("/")+"template/REPORT_TEMPLATES.json"), ReportTemplates.class);
			    
			    ArrayList<Templates> tmps = repTemplate.getReportTemplates();
			    
			    for (Templates tmpDefs : tmps) {
	                templateName = tmpDefs.getTemplatename();
	                templateDef = tmpDefs.getTemplatedef();
	                map.put(templateName, templateDef);
	                
	                System.out.println("ReportGen3-->"+templateName); 
	                
	            }
			    
			  //  HashMap<String, TemplateDef> templateDef1 = (HashMap<String, TemplateDef>) map.get(templateName);
			    
				TemplateDef tempDef = (TemplateDef) map.get(templateName);

				// Current time capture
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();

				// File name generation
				String fileName = templateName + "_" + dtf.format(now);
				fileName = fileName.replaceAll("/", "");
				fileName = fileName.replaceAll(":", "");
				fileName = fileName.replaceAll(" ", "_");

				if (tempDef != null) {

					String reportName = tempDef.getReportname();
					String group = tempDef.getGroup();
					String subtotal = tempDef.getSubtotal();
					String total = tempDef.getTotal();
					boolean filtersReq = tempDef.isFilters();
					boolean tableReq = tempDef.isTable();
					boolean ChartReq = tempDef.isChart();
					boolean customerlogoReq = tempDef.isCustomerlogo();
					boolean productlogoReq = tempDef.isProductlogo();
					boolean generatedtimeReq = tempDef.isGeneratedtime();
					String generatedtimelocation = tempDef.getGeneratedtimelocation();
					boolean footerReq = tempDef.isFooter();
					boolean pagenoReq = tempDef.isPageno();

					// If PDF report
					if (exportType.equalsIgnoreCase("PDF")) {
						// NORMAL
						StyleBuilder normalStyle = stl.style();
						StyleBuilder normalCenteredStyle = stl.style(normalStyle)
								.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

						// BOLD
						StyleBuilder boldStyle = stl.style().bold();
						StyleBuilder boldCenteredStyle = stl.style(boldStyle)
								.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

						// Italic
						StyleBuilder italicStyle = stl.style().italic();
						StyleBuilder italicCenteredStyle = stl.style(italicStyle)
								.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

						// Column Title
						StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle).setBorder(stl.pen1Point())
								.setBackgroundColor(Color.LIGHT_GRAY);
						StyleBuilder titleStyle = stl.style(boldCenteredStyle)
								.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(8);

						// Column Style
						StyleBuilder columnStyle = stl.style(normalCenteredStyle)
								.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(6);

						// Small Title
						StyleBuilder smalltitleStyle = stl.style(normalCenteredStyle)
								.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(8);

						// Small Title
						StyleBuilder italictitleStyle = stl.style(italicCenteredStyle)
								.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(5);

						TextColumnBuilder<String>[] columns = new TextColumnBuilder[reportColumns.size()];
						String[] arrayHeadercolumns = new String[reportColumns.size()];
						String headerCloumns = null;
						String header = null;
						List<String> headerList = new ArrayList();

						for (int i = 0; i < reportColumns.size(); i++) {
							JsonObject jObj = (JsonObject) reportColumns.get(i);
							headerCloumns = jObj.get("field").getAsString();
							header = jObj.get("headerName").getAsString();

							columns[i] = col.column(headerCloumns.replaceAll("_", " "), header, type.stringType());
							arrayHeadercolumns[i] = header;
							headerList.add(headerCloumns);
						}

						DRDataSource dataSource = new DRDataSource(arrayHeadercolumns);
						Object[] row = new Object[headerList.size()];
						int loop = 0;
						for (int i = 0; i < reportDatas.size(); i++) {
							JsonObject jObj = (JsonObject) reportDatas.get(i);
							loop = 0;
							for (String key : headerList) {
								if (jObj != null) {
									if (jObj.get(key) != null) {
										row[loop] = jObj.get(key).getAsString();
										loop++;
									}
								}
							}
							dataSource.add(row);
						}

						response.setContentType("application/pdf");
						response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
						response.setHeader("filename", fileName + ".pdf");

						JasperPdfExporterBuilder pdfExporter = export.pdfExporter(os).setEncrypted(true);
						report()// create new report design
								.setColumnTitleStyle(columnTitleStyle).setSubtotalStyle(boldStyle).columns(columns)
								.setColumnStyle(columnStyle)
								.title(cmp.horizontalList().add(
										cmp.image(imgPath + "logo-3d.png").setFixedDimension(80, 80),
										cmp.text(reportName).setStyle(titleStyle)
												.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT),
										cmp.text("Report Generated at - " + dtf.format(now)).setStyle(smalltitleStyle)
												.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT),
										cmp.image(imgPath + "Dt360_logo.png").setFixedDimension(80, 80)).newRow()
										.add(cmp.filler().setStyle(stl.style().setTopBorder(stl.pen2Point()))
												.setFixedHeight(10)),
										cmp.horizontalList().setStyle(stl.style(10)).setGap(10)
												.add(cmp.hListCell(createCustomerComponent("Filters", reportFilters,
														smalltitleStyle)).heightFixedOnTop()),
										cmp.verticalGap(10))
								.pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle),
										cmp.text("Generated from HOS").setStyle(italictitleStyle)
												.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
								.setDataSource(dataSource).toPdf(pdfExporter);

					} else if (exportType.equalsIgnoreCase("XLS")) {
						StyleBuilder boldStyle = stl.style().bold();
						StyleBuilder boldCenteredStyle = stl.style(boldStyle)
								.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
						StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle).setBorder(stl.pen1Point())
								.setBackgroundColor(Color.LIGHT_GRAY);
						StyleBuilder titleStyle = stl.style(boldCenteredStyle)
								.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(15);

						TextColumnBuilder<String>[] columns = new TextColumnBuilder[reportColumns.size()];
						String[] arrayHeadercolumns = new String[reportColumns.size()];
						String headerCloumns = null;
						String header = null;
						List<String> headerList = new ArrayList();

						for (int i = 0; i < reportColumns.size(); i++) {
							JsonObject jObj = (JsonObject) reportColumns.get(i);
							headerCloumns = jObj.get("field").getAsString();
							header = jObj.get("headerName").getAsString();

							columns[i] = col.column(headerCloumns.replaceAll("_", " "), header, type.stringType());
							columns[i] = col.column(headerCloumns, header, type.stringType())
									.addProperty("net.sf.jasperreports.export.xls.auto.fit.row", "true")
									.addProperty("net.sf.jasperreports.export.xls.auto.fit.column", "true")
									.setHeight(100);
							arrayHeadercolumns[i] = header;
							headerList.add(headerCloumns);
						}

						DRDataSource dataSource = new DRDataSource(arrayHeadercolumns);
						Object[] row = new Object[headerList.size()];
						int loop = 0;
						for (int i = 0; i < reportDatas.size(); i++) {
							JsonObject jObj = (JsonObject) reportDatas.get(i);
							loop = 0;
							for (String key : headerList) {
								//System.out.println("key==="+key);
								if(jObj.get(key)!=null) {
									row[loop] = jObj.get(key).getAsString();
									loop++;
								}
							}
							dataSource.add(row);
						}

						response.setContentType("application/vnd.ms-excel");
						response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
						response.setHeader("filename", fileName + ".xls");

						JasperXlsExporterBuilder xlsExporter = export.xlsExporter(os).setDetectCellType(true)
								.setIgnorePageMargins(false).setWhitePageBackground(false).setOnePagePerSheet(false)
								.setRemoveEmptySpaceBetweenColumns(true).sheetNames("Data");

						report()// create new report design
								.setColumnTitleStyle(columnTitleStyle).setSubtotalStyle(boldStyle).columns(columns)
								.title(cmp.horizontalList().add(
										cmp.image(imgPath + "logo-3d.png").setFixedDimension(80, 80),
										cmp.text(reportName).setStyle(titleStyle)
												.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT),
										cmp.text("Report Generated at - " + dtf.format(now)).setStyle(titleStyle)
												.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT),
										cmp.image(imgPath + "Dt360_logo.png").setFixedDimension(80, 80)).newRow()
										.add(cmp.filler().setStyle(stl.style().setTopBorder(stl.pen2Point()))
												.setFixedHeight(10)))
								.addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2").ignorePageWidth()
								.ignorePagination().setDataSource(dataSource).toXls(xlsExporter);
					} else {
						System.out.println("ErrOR ::  Report type not supported !... " + exportType);
					}
				} else {
					System.out.println("ErrOR ::  Report template missing !...");
				}
			} else {
				System.out.println("ErrOR ::  Report type can't be NULL !... ");
			}

			os.flush();
			os.close();

		} catch (DRException e) {
			e.printStackTrace();
		}
	}

	private ComponentBuilder<?, ?> createCustomerComponent(String label, JsonObject reportFilters,
			StyleBuilder boldStyle) {

		HorizontalListBuilder list = cmp.horizontalList()
				.setBaseStyle(stl.style().setTopBorder(stl.pen1Point()).setLeftPadding(10));
		Set<Entry<String, JsonElement>> entrySet = reportFilters.entrySet();
		String key = null;
		String value = null;
		String[] iglist = new String[] { "userId", "roleId", "siteIDs" };
		List<String> ignoreList = Arrays.asList(iglist);
		// "userId":"AP_11","roleId":"TM","fromDate":"2020-12-08","toDate":"2020-12-08","country":"OMAN","siteIDs":"10101013"
		for (Map.Entry<String, JsonElement> entry : entrySet) {
			key = entry.getKey();
			if (!reportFilters.get(entry.getKey()).isJsonNull())
				value = reportFilters.get(entry.getKey()).getAsString();

			if (!ignoreList.contains(key) && value != null)
				addCustomerAttribute(list, key.toUpperCase(), value, boldStyle);

			value = null;
			key = null;
		}

		// addCustomerAttribute(list, "From Date", "2020-12-15",boldStyle);
		// addCustomerAttribute(list, "To Date", "2020-12-15",boldStyle);
		return cmp.verticalList(cmp.text(label).setStyle(boldStyle), list);

	}

	private void addCustomerAttribute(HorizontalListBuilder list, String label, String value, StyleBuilder boldStyle) {
		if (value != null) {
			list.add(cmp.text(label + ":").setFixedColumns(8).setStyle(boldStyle), cmp.text(value)).newRow();
		}

	}

}
