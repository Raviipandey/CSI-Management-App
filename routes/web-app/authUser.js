const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");

module.exports = {
  post: (req, res) => {
    var username = req.body.username;
    var password = req.body.password;
    //will only allow admin to login, for everyone else it will be invalid
    connection.query("select * from core_details where (core_role_id = 1 or core_role_id = 2 or core_role_id = 3 or core_role_id = 12) and core_id = ? and core_pwd = ?",[username,password],function(error,results,fields){
        if (results.length > 0) {
            // console.log(results);
            var data = JSON.parse(JSON.stringify(results));
            // console.log(data[0].role); 
            var session = req.session;
            session.userid = req.body.username;
            session.userrole = data[0].core_role_id;
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
