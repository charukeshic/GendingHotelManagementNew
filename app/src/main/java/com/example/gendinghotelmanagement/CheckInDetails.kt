package com.example.gendinghotelmanagement

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime


class CheckInDetails : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView


    lateinit var txtCustName: TextView
    lateinit var txtCustIC: TextView
    lateinit var txtCustPhone: TextView
    lateinit var txtRoomType: TextView
    lateinit var txtNoOfRoom: TextView
    lateinit var txtExtraServices: TextView
    lateinit var txtRoomStatus: TextView
    lateinit var txtRoomNum: TextView
    lateinit var txtStaffName: TextView


    lateinit var btnCheckInRoom: Button
    lateinit var checkInData: DatabaseReference
    lateinit var roomData: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_details)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)


        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        btnCheckInRoom = findViewById(R.id.btnCheckInRoom);
        btnCheckInRoom.setOnClickListener { // Do some work here

            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.check_in_dialog,null)
            dialog.setView(dialogView)
            dialog.setCancelable(false)
            dialog.setPositiveButton("Confirm",{dialogInterface: DialogInterface, i:Int -> })
            dialog.setNegativeButton("Cancel", {dialogInterface: DialogInterface, i:Int -> })
            val customDialog = dialog.create()
            customDialog.show()


            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val username=intent.getStringExtra("Username")
                val customer = intent.getStringExtra("CustomerID")
                val staffName = dialogView.findViewById<EditText>(R.id.dialogStaffName).text.toString()
                val roomKey = dialogView.findViewById<EditText>(R.id.dialogRoomKey).text.toString()
                val currentDateTime = LocalDateTime.now()

                if(roomKey.trim().length <= 1) {
                    Toast.makeText(baseContext, "Please enter room key", Toast.LENGTH_SHORT).show()

                }
                else if(staffName.trim().length <= 1) {
                    Toast.makeText(baseContext, "Please enter staff in charge", Toast.LENGTH_SHORT).show()
                }
                else {


                    customer?.let { FirebaseDatabase.getInstance().getReference("CheckIn").child(it).child("roomKey").setValue(roomKey) }!!
                    customer?.let { FirebaseDatabase.getInstance().getReference("CheckIn").child(it).child("staffName").setValue(staffName) }!!
                    customer?.let { FirebaseDatabase.getInstance().getReference("CheckIn").child(it).child("roomStatus").setValue("Checked In on " + currentDateTime) }!!

                    UpdateRoom()

                    Toast.makeText(baseContext, "Room Status Updated", Toast.LENGTH_SHORT).show()

                    customDialog.dismiss()
                    val intent = Intent(this@CheckInDetails, ManagerStaffPortal::class.java)
                    intent.putExtra("Username",username)
                    startActivity(intent)

                }

            }
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                customDialog.dismiss()
            }


        }



        txtCustName = findViewById(R.id.txtCustName)
        txtCustIC = findViewById(R.id.txtCustIC)
        txtCustPhone = findViewById(R.id.txtCustPhone)
        txtRoomType = findViewById(R.id.txtRoomType)
        txtNoOfRoom = findViewById(R.id.txtNoOfRoom)
        txtExtraServices = findViewById(R.id.txtExtraServices)
        txtRoomStatus = findViewById(R.id.txtRoomStatus)
        txtRoomNum = findViewById(R.id.txtRoomNum)
        txtStaffName = findViewById(R.id.txtStaffName)

        val customer = intent.getStringExtra("CustomerID")

        //checkInData = FirebaseDatabase.getInstance().getReference("CheckIn").child(customer)

        checkInData = customer?.let { FirebaseDatabase.getInstance().getReference("CheckIn").child(it) }!!
        checkInData.addValueEventListener(object: ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("customerName").getValue().toString()
                val ic = snapshot.child("customerID").getValue().toString()
                val phone = snapshot.child("phone").getValue().toString()
                val roomType = snapshot.child("roomType").getValue().toString()
                intent.putExtra("typeOfRoom",roomType)
                val noOfRoom = snapshot.child("noOfRoom").getValue().toString()
                intent.putExtra("howManyRoom",noOfRoom)
                val extraServices = snapshot.child("extraServices").getValue().toString()
                val roomStatus = snapshot.child("roomStatus").getValue().toString()
                val roomNum = snapshot.child("roomKey").getValue().toString()
                val staff = snapshot.child("staffName").getValue().toString()

                txtCustName.setText("Customer Name : " + name)
                txtCustIC.setText("Customer ID : " +  ic)
                txtCustPhone.setText("Contact No. : " + phone)
                txtRoomType.setText("Room Type : " + roomType)
                txtNoOfRoom.setText("No Of Room : "+ noOfRoom)
                txtExtraServices.setText("Extra Services : "+ extraServices)
                txtRoomStatus.setText("Room Status : "+ roomStatus)
                txtRoomNum.setText("Room No. : "+ roomNum)
                txtStaffName.setText("Staff In Charge : "+ staff)



            }


        })



    }

    private fun UpdateRoom() {

        val roomType = intent.getStringExtra("typeOfRoom")
        val howManyRoom = intent.getStringExtra("howManyRoom").toString().toInt()
        roomData = roomType?.let { FirebaseDatabase.getInstance().getReference("Room").child(it) }!!
        roomData.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val available = snapshot.child("Available").getValue().toString().toInt()
                val currentAvailable = available.minus(howManyRoom)
                val occupied = snapshot.child("Occupied").getValue().toString().toInt()
                val currentOccupied = occupied.plus(howManyRoom)

                roomType?.let { FirebaseDatabase.getInstance().getReference("Room").child(it).child("Available").setValue(currentAvailable) }!!
                roomType?.let { FirebaseDatabase.getInstance().getReference("Room").child(it).child("Occupied").setValue(currentOccupied) }!!



            }


        })


    }


    override fun onNavigationItemSelected(item: MenuItem):Boolean{
        val username=intent.getStringExtra("Username")
        when (item.itemId){
            R.id.ic_profile -> {
                val intent = Intent (this@CheckInDetails, ManagerStaffPortal::class.java)
                intent.putExtra("Username",username)
                startActivity(intent);
                Toast.makeText(this,"Profile clicked",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_booking -> {
                val intent = Intent (this@CheckInDetails, OrderDetails::class.java)
                intent.putExtra("Username",username)
                startActivity(intent);
                Toast.makeText(this,"Booking clicked",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_activity -> {
                val intent = Intent (this@CheckInDetails, CustomerActivity::class.java)
                intent.putExtra("Username",username)
                startActivity(intent);
                Toast.makeText(this,"Customer Activity clicked",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_operation -> {
                val intent = Intent (this@CheckInDetails, CheckRoomOccupancy::class.java)
                intent.putExtra("Username",username)
                startActivity(intent);
                Toast.makeText(this,"Operation clicked",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut();
                val intent = Intent (this@CheckInDetails, Login::class.java)
                intent.putExtra("Username",username)
                startActivity(intent);
                Toast.makeText(this,"You have successfully sign out",Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}