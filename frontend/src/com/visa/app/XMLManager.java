package com.visa.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class XMLManager {

    private static void appendTextElement(Document doc, Element parent, String tagName, String text) {
        Element el = doc.createElement(tagName);
        el.appendChild(doc.createTextNode(text != null ? text : ""));
        parent.appendChild(el);
    }

    public static boolean exportApplicationToXML(VisaApplication app, File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("VisaApplication");
            rootElement.setAttribute("id", String.valueOf(app.getId()));
            rootElement.setAttribute("status", app.getStatus());
            doc.appendChild(rootElement);

            buildApplicationElement(doc, rootElement, app);

            // Write XML to File
            writeXmlDocToFile(doc, file);
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting single application to XML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportAllApplicationsToXML(List<VisaApplication> apps, File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("VisaApplications");
            doc.appendChild(rootElement);

            for (VisaApplication app : apps) {
                Element appElement = doc.createElement("VisaApplication");
                appElement.setAttribute("id", String.valueOf(app.getId()));
                appElement.setAttribute("status", app.getStatus());
                rootElement.appendChild(appElement);
                
                buildApplicationElement(doc, appElement, app);
            }

            // Write XML to File
            writeXmlDocToFile(doc, file);
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting applications bulk to XML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void buildApplicationElement(Document doc, Element parentElement, VisaApplication app) {
        // Personal Information
        Element personalInfo = doc.createElement("PersonalInformation");
        parentElement.appendChild(personalInfo);
        appendTextElement(doc, personalInfo, "FullName", app.getFullName());
        appendTextElement(doc, personalInfo, "Sex", app.getSex());
        appendTextElement(doc, personalInfo, "Citizenship", app.getCitizenship());
        appendTextElement(doc, personalInfo, "CivilStatus", app.getCivilStatus());
        appendTextElement(doc, personalInfo, "BirthDate", app.getBirthDate());
        appendTextElement(doc, personalInfo, "PlaceOfBirth", app.getPlaceOfBirth());
        appendTextElement(doc, personalInfo, "Email", app.getEmail());
        appendTextElement(doc, personalInfo, "ContactNumber", app.getContactNumber());
        appendTextElement(doc, personalInfo, "HomeAddress", app.getHomeAddress());

        // Family Information
        Element familyInfo = doc.createElement("FamilyInformation");
        parentElement.appendChild(familyInfo);
        appendTextElement(doc, familyInfo, "FatherName", app.getFatherName());
        appendTextElement(doc, familyInfo, "MotherName", app.getMotherName());
        appendTextElement(doc, familyInfo, "SpouseName", app.getSpouseName());
        appendTextElement(doc, familyInfo, "WithChildren", String.valueOf(app.isWithChildren()));

        if (app.isWithChildren() && app.getChildren() != null && !app.getChildren().isEmpty()) {
            Element childrenElement = doc.createElement("Children");
            familyInfo.appendChild(childrenElement);
            for (Child child : app.getChildren()) {
                Element childElement = doc.createElement("Child");
                childrenElement.appendChild(childElement);
                appendTextElement(doc, childElement, "Name", child.getName());
                appendTextElement(doc, childElement, "Age", String.valueOf(child.getAge()));
            }
        }

        // Employment Information
        Element employmentInfo = doc.createElement("EmploymentInformation");
        parentElement.appendChild(employmentInfo);
        appendTextElement(doc, employmentInfo, "Occupation", app.getOccupation());
        appendTextElement(doc, employmentInfo, "EmployerOfficeAddress", app.getEmployerAddress());

        // Travel Documents
        Element travelDocuments = doc.createElement("TravelDocuments");
        parentElement.appendChild(travelDocuments);

        if (app.getDocuments() != null) {
            for (com.visa.app.Document docModel : app.getDocuments()) {
                Element docElement = doc.createElement("Document");
                travelDocuments.appendChild(docElement);
                
                appendTextElement(doc, docElement, "Type", docModel.getDocumentType());
                if ("Original Passport".equalsIgnoreCase(docModel.getDocumentType())) {
                    appendTextElement(doc, docElement, "PassportNumber", docModel.getPassportNumber());
                    appendTextElement(doc, docElement, "IssuingAuthority", docModel.getIssuingAuthority());
                    appendTextElement(doc, docElement, "DateIssued", docModel.getDateIssued());
                    appendTextElement(doc, docElement, "ValidityDate", docModel.getValidityDate());
                }
            }
        }
    }

    private static void writeXmlDocToFile(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
}
