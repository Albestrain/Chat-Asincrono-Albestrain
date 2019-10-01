
package server_messenger_aldo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class BaseDeDatos {
String usuario, contraseña;
Connection connselect;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    //Metodo para abrir conección
    public void abrirConeccion() throws ClassNotFoundException, SQLException{
        Class.forName("org.sqlite.JDBC");
        connselect = DriverManager.getConnection("jdbc:sqlite:C:/bd-albestrain/usuarios");
        
    }
    
    //Metodos para la tabla cuentas de los usuarios
    
    public boolean UsuarioExistente(String user) throws SQLException{
        String select_usuario = "select count(usuario) from cuentas where usuario = ?;";
        PreparedStatement slcs = connselect.prepareStatement(select_usuario);
        slcs.setString(1, user);
        ResultSet rsS = slcs.executeQuery();
        System.out.println(rsS.getInt(1));
        if(rsS.getInt(1) > 0){
            return true;    
        }else{
            return false;
        }
       
    }
    
    public boolean contraseñaCorrecta(String user, String contrasena) throws SQLException{
        String select_usuario = "select  count(usuario) from cuentas where usuario = ? AND password = ?;";
        PreparedStatement slcs = connselect.prepareStatement(select_usuario);
        slcs.setString(1, user);
        slcs.setString(2, contrasena);
        ResultSet rsS = slcs.executeQuery();
        if(rsS.getInt(1) > 0){
            return true;    
        }else{
            return false;
        }
    }
    
    public void insertarUsuario(String usuario, String contrasena) throws SQLException{
        String insercionSQL = "insert into cuentas values(?,?);";
        PreparedStatement ps = connselect.prepareStatement(insercionSQL);
        ps.setString(1, usuario);
        ps.setString(2, contrasena);
        ps.executeUpdate();
    }
    
    //Metodos para la tabla bloqueados
    
    public void insertarBloqueados(String usuario, String usuarioBloqueado) throws SQLException{
        String insercionSQL = "insert into bloqueados values(?,?);";
        PreparedStatement ps = connselect.prepareStatement(insercionSQL);
        ps.setString(1, usuario);
        ps.setString(2, usuarioBloqueado);
        ps.executeUpdate();
    }
    
    public void eliminarBloqueados(String usuario, String usuarioBloqueado) throws SQLException{
        String insercionSQL = "delete from bloqueados where usuario = ? AND usuarioBloqueado = ?;";
        PreparedStatement ps = connselect.prepareStatement(insercionSQL);
        ps.setString(1, usuario);
        ps.setString(2, usuarioBloqueado);
        ps.executeUpdate();
    }
    
    public boolean meTieneBloqueado(String usuario, String usuarioBloqueado) throws SQLException{
         String select_usuario = "select  count(usuario) from bloqueados where usuario = ? AND usuarioBloqueado = ?;";
        PreparedStatement slcs = connselect.prepareStatement(select_usuario);
        slcs.setString(1, usuario);
        slcs.setString(2, usuarioBloqueado);
        ResultSet rsS = slcs.executeQuery();
        if(rsS.getInt(1) > 0){
            return true;    
        }else{
            return false;
        }
    }
    
    //metodo para Cerrar conección
    public void cerrarConeccion() throws SQLException{
        connselect.close();
    }
    
}
