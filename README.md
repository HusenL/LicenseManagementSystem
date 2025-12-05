# Foreign Trade License Management System

A desktop application built in **Java (JavaFX + FXML + CSS)** with **MySQL database** integration to manage and streamline foreign trade operations.

This system allows:

1. **Registering Exporters**
2. **Issuing Licenses**
3. **Logging Shipment Entries**
4. **Filtering Shipments by License**
5. **Using a Trade Compliance Chatbot UI**

> ⚠ Current version supports data creation and viewing only.  
> Editing/updating/deleting licenses or shipments is not yet available.

---

## 📌 Features & Screens

### 1️⃣ Exporter Registration
- Firm Name  
- IEC Number *(10 digits)*  
- Contact Person  
- Country  
➡ Saves exporter for license issuance.

---

### 2️⃣ License Issuance
- Select IEC from dropdown  
- Auto-fills exporter information  
- Enter expiry days  
➡ Generates a unique License Number.

---

### 3️⃣ Shipment Entry
- License Reference (dropdown)  
- Product Name  
- Quantity (Tonnes)  
- Destination  
- Cost ($)  
- Export Date (picker)  
- Insurance checkbox  
➡ Shipment entry stored in MySQL.

---

### 4️⃣ Shipment Filter
Filters shipments based on license reference.  
Display table includes:

| ID | Product | Destination | Status | Cost($) | Insured | Timestamp |

---

### 5️⃣ Trade Compliance Chatbot UI
Basic UI for compliance queries.  
Contains:
- Chat display area
- User input field
- Send button  
➡ Chatbot logic/AI coming in future.

---

## 🚫 Current Limitations

| Available | Not Available Yet |
|-----------|-------------------|
| Add exporters | Edit exporters |
| Issue licenses | Update/Delete licenses |
| Log shipments | Update/Delete shipments |
| Filter shipments | User authentication |
| Chatbot UI screen | Chatbot response AI |

---

## 🔮 Future Enhancements

- Edit / Delete licenses  
- Edit / Delete shipments  
- AI-powered chatbot  
- Export reports to PDF/Excel  
- Login system (Admin/User roles)  
- Dashboard analytics  

---

## 🛠 Tech Stack

| Component | Technology |
|----------|------------|
| Language | Java |
| UI | JavaFX + FXML |
| Styling | CSS |
| Database | MySQL |
| IDE | IntelliJ IDEA |

---

## 🚀 How to Run the Project

### 1. Clone Repository
```bash
git clone https://github.com/HusenL/LicenseManagementSystem.git
cd LicenseManagementSystem

2. Open in IntelliJ IDEA

File → Open
Select project folder
Let Gradle/JavaFX index properly

3. Configure JavaFX

Download JavaFX SDK → https://gluonhq.com/products/javafx/
IntelliJ → File → Project Structure → Libraries → + → Java
Select JavaFX/lib folder
Add VM options before running:
             --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
```

4. MySQL Database Setup

CREATE DATABASE trade_license_system;
USE trade_license_system;

CREATE TABLE exporters (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firm_name VARCHAR(255),
    iec VARCHAR(10) UNIQUE,
    contact_person VARCHAR(255),
    country VARCHAR(100)
);

CREATE TABLE licenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exporter_id INT,
    license_number VARCHAR(50),
    expiry_days INT,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    license_ref VARCHAR(50),
    product VARCHAR(255),
    destination VARCHAR(255),
    status VARCHAR(50),
    cost DECIMAL(10,2),
    insured BOOLEAN,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```
Update DB credentials in code:
username = "root"
password = "your-password"
database = "trade_license_system"

5. Run the Application
Open: Main.java
```

```
## 👤 Author

**Husen Lakdawala**  
🔗 GitHub: https://github.com/HusenL


If this project helped you, please consider giving the repo a Star!
