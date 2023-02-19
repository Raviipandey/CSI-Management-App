const mysql = require('mysql');
const connection = mysql.createPool({

    // host: "localhost",
    // user: "root",
    // password: "",
    // database: "csi_demo",
    host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022',
    connectionLimit : 100,
});
connection.getConnection((err)=>{
    if (err) throw err;    
    console.log("Web DB connected successfully");
});

module.exports = connection; 