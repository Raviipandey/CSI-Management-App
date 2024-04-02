const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const crypto = require('crypto');

module.exports = {
  post: (req, res) => {
    var username = req.body.username;
    var password = req.body.password;
    password = crypto.createHash('md5').update(password).digest('hex');
    //will only allow admin to login, for everyone else it will be invalid
    
    connection.query("SELECT CD.*, CR.role_name FROM core_details CD INNER JOIN core_role_master CR ON CD.core_role_id = CR.role_id WHERE CD.core_role_id IN (1, 2, 3, 12) AND CD.core_id = ? AND CD.core_pwd = ?;",[username,password],function(error,results,fields){
        if (results.length > 0) {
            // console.log(results);
            var data = JSON.parse(JSON.stringify(results));
            // console.log(data[0].role); 
            var session = req.session;
            session.userid = req.body.username;
            session.userrole = data[0].core_role_id;
            session.rolename = data[0].role_name;
            res.redirect("/dashboard");
            req.session.isAuth = true;
            console.log(req.session);
            console.log(req.session.id);
        } else {
            req.flash("Emsg", "Access Not Granted!");
            res.redirect("/");
        }
        res.end();
    })
}
};
