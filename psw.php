<?php
    $servername = "localhost";
    $username = "root";
    $password = "";
    $dbname = "password_manager";
    $conn = new mysqli($servername, $username, $password, $dbname);

    $fn = $_GET['fn'];

    if($fn == "login"){
        $flag = false;
        if( isset($_GET['username']) && isset($_GET['password'])){
            $user = $_GET['username'];
            $pass = $_GET['password'];
            $sql = "SELECT * FROM user_info WHERE email='$user'";
            $result = $conn->query($sql);
            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                if($pass == $row["password"]){
                    $flag = true;
                    $id = $row["id"];
                }
            }
        }
        if($flag){
            $arr = array('validity' => "true",'id'=>$id);
            echo json_encode($arr);
        }else{
            $arr = array('validity' => "false",'id'=>$id);
            echo json_encode($arr);
        }
    }

    elseif($fn == "signup"){
        if( isset($_GET['username']) && isset($_GET['password'])){
            $user = $_GET['username'];
            $pass = $_GET['password'];

            $sql = "SELECT * FROM user_info WHERE email='$user'";
            $result = $conn->query($sql);
            if ($result->num_rows > 0) {
                $arr = array('validity' => "false", "duplicate" => "true");
                echo json_encode($arr);
            }else{
                $sql = "INSERT INTO user_info(email, password) VALUES('$user','$pass')";
                $arr; 
                if ($conn->query($sql) === TRUE) 
                    $arr = array('validity' => "true", "duplicate" => "");
                else
                    $arr = array('validity' => "false", "duplicate" => "");
                echo json_encode($arr);
            }
        }
    }

    elseif($fn == "get_data"){
        $userid = $_GET['data1'];
        $sql = "SELECT * FROM user_data WHERE userid='$userid'";
        // $sql = "SELECT * FROM user_data";
        $result = $conn->query($sql)->fetch_all(MYSQLI_ASSOC);
        $list = array();
        foreach ($result as $row){
            $list[] = array('rowid' => $row['id'], 'userid' => $row['userid'], 'data1' => $row['data1'], 'data2' => $row['data2'], 'data3' => $row['data3']);
        }
        echo json_encode($list);
    }

    elseif($fn == "save_account"){
        $userid = $_GET['data1'];
        $account_name = $_GET['data2'];
        $username = $_GET['data3'];
        $password = $_GET['data4'];
        $sql = "INSERT INTO user_data(userid, data1, data2, data3) VALUES('$userid','$account_name', '$username', '$password')";
        if ($conn->query($sql) === TRUE) 
        
            $arr = array('validity' => "true");
        else
            $arr = array('validity' => "false");
        echo json_encode($arr);
    }

    elseif($fn == "delete_account"){
        $userid = $_GET['data1'];
        $account_name = $_GET['data2'];
        $username = $_GET['data3'];
        $password = $_GET['data4'];

        $sql = "DELETE FROM user_data WHERE userid='$userid' AND data1='$account_name' AND data2='$username' AND data3='$password'";
        if ($conn->query($sql) === TRUE) 
            $arr = array('success' => "true",'error' => 'NONE');
        else
            $arr = array('success' => "false",'error' => 'sql failed');
        echo json_encode($arr);
    }

    elseif($fn == "edit_account"){
        $userid = $_GET['data1'];
        $account_name = $_GET['data2'];
        $username = $_GET['data3'];
        $password = $_GET['data4'];
        $account_name_new = $_GET['data5'];
        $username_new = $_GET['data6'];
        $password_new = $_GET['data7'];
        $sql = "UPDATE user_data SET data1='$account_name_new', data2='$username_new', data3='$password_new' WHERE userid='$userid' AND data1='$account_name' AND data2='$username' AND data3='$password'";
        if ($conn->query($sql) === TRUE) 
            $arr = array('success' => "true");
        else
            $arr = array('success' => "false");
        echo json_encode($arr);
    }

    elseif($fn="delete_user_data"){
        $userid = $_GET['data1'];
        $sql = "DELETE FROM user_data WHERE userid='$userid'";
        if ($conn->query($sql) === TRUE) 
            $arr = array('success' => "true",'error' => 'NONE');
        else
            $arr = array('success' => "false",'error' => 'sql failed');
        echo json_encode($arr);
    }

?>


<!-- 
    save account
    localhost/psw.php?fn=save_account&data1=bdc5dc4193db3f5021373e61dfbd69ff178097ef&data2=C99AC650F1E73C2B06172A3BAC73C38C&data3=4D9168FACF244CE0B1E0264824D7C165&data4=AE454D4062A8EBD174C5D068ED775511
    http://localhost/psw.php?fn=save_account&data1=12&data2=12&data3=12&data4=12

 -->