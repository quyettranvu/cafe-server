package com.example.server.serviceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.server.config.kafka.producers.OrderEventProducer;
import com.example.server.dto.CustomerOrder;
import com.itextpdf.text.Element;

import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.server.JWT.JwtFilter;
import com.example.server.POJO.Bill;
import com.example.server.constants.ApiConstants;
import com.example.server.dao.BillDao;
import com.example.server.service.BillService;
import com.example.server.utils.CafeUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderEventProducer orderEventProducer;

    public BillServiceImpl(KafkaTemplate<String, Object> kafkaTemplate, OrderEventProducer orderEventProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");
        try {
            String fileName;
            if (validateRequestMap(requestMap)) {
                log.info((String) requestMap.get("fileName"));
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    // generate a new uuid if isGenerate=true
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap); // save to db
                }

                String data = "Name: " + requestMap.get("name") + "\n" + "Contact Number: "
                        + requestMap.get("contactNumber") +
                        "\n" + "Email: " + requestMap.get("email") + "\n" + "Payment Method: "
                        + requestMap.get("paymentMethod");

                // Generate and save bill as pdf, shapes and outside objects
                Document document = new Document();
                PdfWriter.getInstance(document,
                        new FileOutputStream(ApiConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));
                document.open();
                setRectangleInPDf(document);

                // Define text inside the bill
                Paragraph chunk = new Paragraph("Quyet Tran Vu Cafe Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                // table has 5 columns and set headers for each column
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                // add data into table
                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: " + requestMap.get("totalAmount") + "\n"
                        + "Thank you for visiting.Please visit again!!", getFont("Data"));
                document.add(footer);
                document.close();
                return CafeUtils.getResponseEntity("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Required data not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(ApiConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInPDf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPDf");
        Rectangle rect = new Rectangle(577, 825, 18, 15);
        rect.enableBorderSide(1); // top side of border
        rect.enableBorderSide(2); // bottom side
        rect.enableBorderSide(4); // left side
        rect.enableBorderSide(8); // right side
        rect.setBorderColor(BaseColor.BLACK);
        rect.setBorderWidth(1);
        document.add(rect);
    }


    @Caching(evict = {
        @CacheEvict(value = "dashboardCounts", key = "'getCount'"),
        @CacheEvict(value = "getBillList", key = "'getBills'")
    })
    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }

    @Override
    @Cacheable(value="getBillList", key="'getBills'")
    public List<Bill> getBills() {
        List<Bill> list = new ArrayList<>();
        if (jwtFilter.isAdmin()) {
            list = billDao.getAllBills();
        } else {
            list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
        }
        return list;
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf: requestMap {}", requestMap);
        try {
            byte[] byteArray = new byte[0];
            // in request body need to contain value for uuid
            if (!requestMap.containsKey("uuid")) {
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = ApiConstants.STORE_LOCATION + "\\" + (String) requestMap.get("uuid") + ".pdf";
            if (!CafeUtils.isFileExist(filePath)) {
                // if file not existed generate new uuid and new PDF file
                requestMap.put("isGenerate", false);
                generateReport(requestMap); // print bill for customer

                // notify new order for kitchen
                Map<String, String> data = (Map<String, String>) requestMap.get("data");
                CustomerOrder customerOrder = new CustomerOrder((Integer) requestMap.get("uuid"), data.get("name"), data.get("contactNumber"),
                        data.get("paymentMethod"), Integer.parseInt(data.get("totalAmount")), data.get("productDetails"));
                orderEventProducer.sendOrderEvent("orders", customerOrder);
            }
            byteArray = getByteArray(filePath);
            return new ResponseEntity<>(byteArray, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Read contents of PDF file
    private byte[] getByteArray(String filePath) throws Exception {
        File initialFile = new File(filePath);
        InputStream targetStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "dashboardCounts", key = "getCount"),
        @CacheEvict(value = "getBillList", key = "'getBills'")
    })
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional<Bill> optional = billDao.findById(id);
            if (optional.isPresent()) {
                Bill bill = optional.get();
                // admin can delete any bills but users can delete only their bills
                if (jwtFilter.isAdmin()) {
                    billDao.deleteById(id);
                    return CafeUtils.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
                } else {
                    String createdby = bill.getCreatedBy();
                    if (createdby.equals(jwtFilter.getCurrentUser())) {
                        return CafeUtils.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
                    }
                    return CafeUtils.getResponseEntity("You are not authorized to delete this bill",
                            HttpStatus.UNAUTHORIZED);
                }
            }
            return CafeUtils.getResponseEntity("Bill id does not exist", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(ApiConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
