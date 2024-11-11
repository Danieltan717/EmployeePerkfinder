import tkinter as tk
from tkinter import ttk
import os, firebase_admin, random
from firebase_admin import credentials
from firebase_admin import db

root = os.path.dirname(__file__)
json_file = os.path.join(root, 'serviceAccountKey.json') # Use your own service account in Firebase

print(json_file)

cred = credentials.Certificate(json_file)

firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://android-dev-assign-default-rtdb.asia-southeast1.firebasedatabase.app'
})


def generate_employee_id():
    prefix = "EP-"
    while True:
        random_number = random.randint(10000000,99999999)
        new_employee_id = f"{prefix}{random_number}"


        existing_ids = db.reference('users').order_by_key().equal_to(new_employee_id).get()
        if not existing_ids:
            return new_employee_id
        
def submit_employee_data():

    # Get data from entry fields
    first_name = first_name_entry.get()
    last_name = last_name_entry.get()
    address = address_entry.get()
    contact = contact_entry.get()
    

    # Generate a unique key for the new user
    new_employee_id = generate_employee_id()  # Retrieve the generated unique ID

    #Create a new user object
    new_user_id = str(new_employee_id)
    new_user = {
        'first_name': first_name,
        'last_name': last_name,
        'address': address,
        'contact': contact,
        'employee_id': new_user_id
    }


    # Push the new user to the 'users' node in the database with custom ID
    db.reference('users/' + new_user_id).set(new_user)

    # Clear the entry fields
    first_name_entry.delete(0, tk.END)
    last_name_entry.delete(0, tk.END)
    address_entry.delete(0, tk.END)
    contact_entry.delete(0, tk.END)

    # Display the generated unique ID in the employee ID field and a success message
    employeeID_entry.insert(0, new_employee_id)
    success_label.config(text="Employee data submitted successfully!")

    # Display a success message
    success_label.config(text="Employee data submitted successfully!")


# Create the main window
window = tk.Tk()

# Create notebook for tabs
notebook = ttk.Notebook(window)
notebook.pack(expand=True, fill="both")

# Create the Employee tab
employee_tab = ttk.Frame(notebook)
notebook.add(employee_tab, text="Employee")

# Create labels and entry fields for employee data
first_name_label = tk.Label(employee_tab, text="First Name:")
first_name_label.pack()
first_name_entry = tk.Entry(employee_tab)
first_name_entry.pack()

last_name_label = tk.Label(employee_tab, text="Last Name:")
last_name_label.pack()
last_name_entry = tk.Entry(employee_tab)
last_name_entry.pack()

address_label = tk.Label(employee_tab, text="Address:")
address_label.pack()
address_entry = tk.Entry(employee_tab)
address_entry.pack()

contact_label = tk.Label(employee_tab, text="Phone Number:")
contact_label.pack()
contact_entry = tk.Entry(employee_tab)
contact_entry.pack()

employeeID_label = tk.Label(employee_tab, text="Employee ID:")
employeeID_label.pack()
employeeID_entry = tk.Entry(employee_tab, state='readonly')
employeeID_entry.pack()


# Create the submit button
submit_button = tk.Button(employee_tab, text="Submit", command=submit_employee_data)
submit_button.pack()

# Create a label to display success/error messages
success_label = tk.Label(employee_tab, text="")
success_label.pack()

#fetch_latest_employee_id()

# ... (create the Locations tab similarly)

window.geometry("600x600")
window.mainloop()