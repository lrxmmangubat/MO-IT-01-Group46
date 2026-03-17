Program Details

This document describes the Java-based MotorPH Basic Payroll System. The program reads employee details and attendance records from CSV files, processes daily time logs, and computes the total hours worked based on company rules such as standard working hours, grace periods, and time adjustments.

The system calculates payroll for two cutoff periods (June 1–15 and June 16–30). Gross salary is determined by multiplying the total hours worked by the employee’s hourly rate.

For the second cutoff, the program applies mandatory government deductions, including SSS, PhilHealth, and Pag-IBIG contributions. It then computes the taxable income by subtracting these contributions from the total monthly gross salary. Based on this taxable income, the system calculates the withholding tax using the appropriate tax bracket.

The total deductions (government contributions + withholding tax) are applied only in the second cutoff, and the final net salary is computed accordingly. The system provides a structured output showing employee details, hours worked, gross salary, deductions, and net pay.
____________________________________________________________________________________________________________________________________________________________________________________________________
Program Flow Overview

1. User logs into the system.
2. The program verifies the username and password.
3. Depending on the user type (employee or payroll staff), the system displays the appropriate menu.
4. Payroll staff can compute payroll for one employee or all employees.
5. The program reads employee data and attendance records from CSV files.
6. The system calculates total hours worked while applying attendance rules.
7. Gross salary is computed using hours worked and hourly rate.
8. Government contributions (SSS, PhilHealth, Pag-IBIG) are deducted.
9. Taxable income is calculated by subtracting contributions from monthly gross salary.
10. Withholding tax is computed using the BIR tax table.
11. Total deductions and net salary are displayed.

PROJECT PLAN LINK

https://docs.google.com/document/d/1u2a8YHnFSlPc3w0hSNqTf9OgcZWHbYla6CbUjKtqL30/edit?tab=t.0
