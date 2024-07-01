package movies;

import com.fasterxml.jackson.databind.ObjectMapper; //para convertir objetos Java a JSON y viceversa
import javax.servlet.ServletException; 
import javax.servlet.annotation.WebServlet; // anotación que mapea este servlet a una URL específica
import javax.servlet.http.HttpServlet; //para extender esta clase y manejar peticiones HTTP
import javax.servlet.http.HttpServletRequest; //manejar las solicitudes HTTP
import javax.servlet.http.HttpServletResponse; //manejar las respuestas HTTP
import java.io.IOException; 
import java.sql.*; 
import java.util.ArrayList; 
import java.util.List;

@WebServlet("/peliculas")
public class Controlador extends HttpServlet{

     // Método POST para insertar una nueva película desde una solicitud JSON
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configurar cabeceras CORS
        response.setHeader("Access-Control-Allow-Origin", "*"); // Permitir acceso desde cualquier origen
        response.setHeader("Access-Control-Allow-Methods", "*"); 
        response.setHeader("Access-Control-Allow-Headers", "Content-Type"); // Cabeceras permitidas
        Conexion conexion = new Conexion();  // Crear una nueva conexión a la base de datos
        Connection conn = conexion.getConnection();  // Obtener la conexión establecida

        try {
            ObjectMapper mapper = new ObjectMapper();  //convertir JSON a objetos Java
            Pelicula pelicula = mapper.readValue(request.getInputStream(), Pelicula.class);  // Convertir el JSON de la solicitud a un objeto Pelicula
        
            // Consulta SQL para insertar una nueva película en la tabla 'peliculas'
            String query = "INSERT INTO peliculas (titulo, genero, duracion, imagen) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);  // Indicar que queremos obtener las claves generadas automáticamente
        
            statement.setString(1, pelicula.getTitulo());
            statement.setString(2, pelicula.getGenero());
            statement.setString(3, pelicula.getDuracion());
            statement.setString(4, pelicula.getImagen());
        
            statement.executeUpdate();  // inserción en la base de datos
        
            // Obtener las claves generadas automáticamente (el ID de la película)
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                Long idPeli = rs.getLong(1);  // Obtener el valor del primer campo generado automáticamente (en este caso, el ID)
                
                // Devolver el ID de la película insertada como JSON en la respuesta
                response.setContentType("application/json");  // Establecer el tipo de contenido de la respuesta como JSON
                String json = mapper.writeValueAsString(idPeli);  // Convertir el ID a formato JSON
                response.getWriter().write(json);  // Escribir el JSON en el cuerpo de la respuesta HTTP
            }
            
            response.setStatus(HttpServletResponse.SC_CREATED);  // Configurar el código de estado de la respuesta HTTP como 201 (CREATED)

        } catch (SQLException e) {
            e.printStackTrace();  //problemas con la base de datos
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // Configurar el código de estado 500 (INTERNAL_SERVER_ERROR)
        } catch (IOException e) {
            e.printStackTrace();  //problemas de entrada/salida 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // Configurar el código de estado 500 (INTERNAL_SERVER_ERROR)
        } finally {
            conexion.close();  // Cerrar la conexión a la bbdd
        }
        
    }

    // Método GET para obtener todas las películas almacenadas en la base de datos y devolverlas como JSON
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*"); 
        response.setHeader("Access-Control-Allow-Headers", "Content-Type"); 
        Conexion conexion = new Conexion();  // Crear una nueva conexión a la base de datos
        Connection conn = conexion.getConnection();  // Obtener la conexión establecida

        try {
            // Consulta SQL para seleccionar todas las películas de la tabla 'peliculas'
            String query = "SELECT * FROM peliculas";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);  // Ejecutar la consulta y obtener los resultados

            List<Pelicula> peliculas = new ArrayList<>();  

            while (resultSet.next()) {
              
                Pelicula pelicula = new Pelicula(
                    resultSet.getInt("id_pelicula"),
                    resultSet.getString("titulo"),  
                    resultSet.getString("genero"),
                    resultSet.getString("duracion"),
                    resultSet.getString("imagen")
                );
                peliculas.add(pelicula); 
            }

            ObjectMapper mapper = new ObjectMapper();  // Crear un objeto ObjectMapper para convertir objetos Java a JSON
            String json = mapper.writeValueAsString(peliculas);  //Pasar la lista de películas a formato JSON

            response.setContentType("application/json");  // Establecer el tipo de contenido de la respuesta como JSON
            response.getWriter().write(json);  //Escribir el JSON en el cuerpo de la respuesta HTTP

        } catch (SQLException e) {
            e.printStackTrace();  
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  
        } finally {
            conexion.close();  
        }
    }

}