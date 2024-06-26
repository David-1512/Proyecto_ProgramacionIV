package com.example.proyecto_programacioniv.logic;

import com.example.proyecto_programacioniv.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service("service")
public class Service {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private  HaciendaRepository haciendaRepository;
    @Autowired
    private AdministradorRepository administradorRepository;
    @Autowired
    private FacturaRepository facturaRepository;
    @Autowired
    private LineaServicioRepository lineaServicioRepository;

    public Iterable<ClienteEntity> clienteFindAll() { return clienteRepository.findAll(); }

    public Optional<ProveedorEntity> proveedorFindById(String id){
        return proveedorRepository.findById(id);
    }
    public Optional<ProveedorEntity> proveedorFindByIdandContrasena(String id, String contrasena){
        return proveedorRepository.findByIdAndContrasena(id,contrasena);
    }

    public void agregarHacienda(String nif){
        HaciendaEntity hE = new HaciendaEntity(nif, "");
        haciendaRepository.save(hE);
    }
    public void agregarProveedor(ProveedorEntity p, String nif) throws Exception {
        for(AdministradorEntity a: administradorRepository.getListAdministrado()){
            if(Objects.equals(a.getId(), p.getId())){
                throw new Exception("Ya existe un administrador con ese ID");
            }
        }
        if(proveedorRepository.findById(p.getId()).isPresent()){
            throw new Exception("Ya existe un proveedor con ese ID");
        }
        for(ProveedorEntity prov: proveedorRepository.findAll()){
            if(Objects.equals(prov.getCorreo(), p.getCorreo())){
                throw new Exception("Ya existe un proveedor con ese correo");
            }
            if(Objects.equals(prov.getHaciendaByNif().getNif(), nif)){
                throw new Exception("Ya existe un proveedor con ese NIF");
            }
        }
        agregarHacienda(nif);
        HaciendaEntity hE = findHaciendaByNIF(nif);
        p.setHaciendaByNif(hE);
        proveedorRepository.save(p);
        p.getHaciendaByNif().getProveedorsByNif().add(p);
        haciendaRepository.save(p.getHaciendaByNif());
    }

    public HaciendaEntity findHaciendaByNIF(String Nif){
        return haciendaRepository.findByNif(Nif);
    }

    public Optional<ClienteEntity> clienteFindById(String id) {return clienteRepository.findById(id);}

    public Optional<ProductoEntity> productoFindByCod(String cod){return productoRepository.findById(cod);}


    //---------------------------------------- PRODUCTOS ---------------------------------------------
    public List<ProductoEntity> mostrarProductosProveedor(String proveedorID) {
        return (List<ProductoEntity>) proveedorRepository.findById(proveedorID).get().getProductosById();
    }

    public void eliminarProducto(String proveedorID, String productoID) {
        productoRepository.deleteById(productoID);
    }


    public List<ProductoEntity> buscadorProductos(String proveedorID, String searchTerm) {
        Optional<ProveedorEntity> proveedorOptional = proveedorRepository.findById(proveedorID);
        if (proveedorOptional.isPresent()) {
            ProveedorEntity proveedor = proveedorOptional.get();
            List<ProductoEntity> productos = new ArrayList<>(proveedor.getProductosById());

            return productos.stream()
                    .filter(producto -> producto.getNombre().toLowerCase().contains(searchTerm.toLowerCase())
                            || producto.getCod().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public ProductoEntity buscarProductoPorCodigo(String proveedorID, String productoID) {
        Optional<ProveedorEntity> proveedorOptional = proveedorRepository.findById(proveedorID);
        if (proveedorOptional.isPresent()) {
            ProveedorEntity proveedor = proveedorOptional.get();
            return proveedor.getProductosById().stream()
                    .filter(producto -> producto.getCod().equalsIgnoreCase(productoID))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void agregarProducto(String cod, String nom, double precio, String proveedorID, boolean esProducto) throws Exception {
        if (productoRepository.findById(cod).isPresent() ) {
            if(esProducto) {
                ProductoEntity producto = productoRepository.findById(cod).get();
                producto.setPrecio(precio);
                producto.setNombre(nom);
                productoRepository.save(producto);
            }
            else throw new Exception("Codigo ya existe");
        }
        else{
            ProveedorEntity prov= proveedorRepository.findById(proveedorID).get();
            ProductoEntity pE = new ProductoEntity(cod,nom,precio,prov);
            productoRepository.save(pE);
            throw new Exception("Se agregó producto");
        }
        throw new Exception("no se agregó producto");
    }
    //-----------------------------------------------------------------------------------------------------------
    public Iterable<ProveedorEntity> proveedorFindAll(){
        return proveedorRepository.findAll();
    }


    public void proveedorDelete(String id) {
        proveedorRepository.deleteById(id);
    }

    public Iterable<ProveedorEntity> proveedorRegistrados() {
        List<ProveedorEntity> list = (List<ProveedorEntity>) proveedorRepository.findAll();

        List<ProveedorEntity> list2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getEstado() != 'E') {
                list2.add(list.get(i));
            }
        }
        return  list2;
    }

    public List<ProveedorEntity> proveedoresNuevos() {
        List<ProveedorEntity> list = (List<ProveedorEntity>) proveedorRepository.findAll();

        List<ProveedorEntity> list2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getEstado() == 'E') {
                list2.add(list.get(i));
            }
        }
        return list2;
    }

    public void aceptarProveedor(String id) {
        ProveedorEntity proveedor = proveedorRepository.findById(id).get();
        proveedor.setEstado('D');
        proveedorRepository.save(proveedor);
    }
    public void activarProveedor(String id) {
        ProveedorEntity proveedor = proveedorRepository.findById(id).get();
        proveedor.setEstado('A');
        proveedorRepository.save(proveedor);
    }

    public void desactivarProveedor(String id) {
        ProveedorEntity proveedor = proveedorRepository.findById(id).get();
        proveedor.setEstado('I');
        proveedorRepository.save(proveedor);
    }

    public List<ProveedorEntity> buscarProveedorPor(String searchTerm) {
            List<ProveedorEntity> provedores = (List<ProveedorEntity>) proveedorRegistrados();

            return provedores.stream()
                    .filter(prov -> (prov.getNombre().toLowerCase().contains(searchTerm.toLowerCase())
                            || prov.getId().toLowerCase().contains(searchTerm.toLowerCase())))
                    .collect(Collectors.toList());
    }

    public Object buscarProveedorNuevoPor(String searchTerm) {
        List<ProveedorEntity> provedores = proveedoresNuevos();

        return provedores.stream()
                .filter(prov -> (prov.getNombre().toLowerCase().contains(searchTerm.toLowerCase())
                        || prov.getId().toLowerCase().contains(searchTerm.toLowerCase())))
                .collect(Collectors.toList());
    }


    //------------------------------------CLIENTES--------------------------------------------
    public List<ClienteEntity> mostrarClientesDeProveedor(String proveedorID) {
        List<ClienteEntity> ls = (List<ClienteEntity>) proveedorRepository.findById(proveedorID).get().getClientesById();
        return ls;
    }

    public List<ClienteEntity> buscarClientesProveedor(String proveedorID, String busqueda) {
        Optional<ProveedorEntity> proveedorOptional = proveedorRepository.findById(proveedorID);
        if (proveedorOptional.isPresent()) {
            ProveedorEntity proveedor = proveedorOptional.get();
            List<ClienteEntity> clientes = new ArrayList<>(proveedor.getClientesById());

            return clientes.stream()
                    .filter(cliente -> cliente.getNombre().toLowerCase().contains(busqueda.toLowerCase())
                            || cliente.getId().toLowerCase().contains(busqueda.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public ClienteEntity seleccionarCliente(String proveedorID, String clienteID) {
        Optional<ProveedorEntity> proveedorOptional = proveedorRepository.findById(proveedorID);
        if (proveedorOptional.isPresent()) {
            ProveedorEntity proveedor = proveedorOptional.get();
            return proveedor.getClientesById().stream()
                    .filter( cliente -> cliente.getId().equalsIgnoreCase(clienteID))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void agregarCliente(String clienteID, String nombre, String correo, String telefono, boolean esCliente, String proveedorID) throws Exception {
        for(ClienteEntity clien: clienteRepository.findAll()){
            if(Objects.equals(clien.getCorreo().toLowerCase(), correo.toLowerCase())){
                throw new Exception("Ya el correo se encuentra registrado");
            }
        }

        if (clienteRepository.findById(clienteID).isPresent()) {
            if(esCliente) {
                ClienteEntity cliente = clienteRepository.findById(clienteID).get();
                cliente.setNombre(nombre);
                cliente.setCorreo(correo);
                cliente.setTelefono(telefono);
                clienteRepository.save(cliente);
                throw new Exception("Se logró editar");
            }
            else throw new Exception("Ya el ID se encuentra registrado");
        }
        else{
            ProveedorEntity prov = proveedorRepository.findById(proveedorID).get();
            ClienteEntity pE = new ClienteEntity(clienteID, nombre, correo, telefono, prov);
            clienteRepository.save(pE);
            throw new Exception("Se guardo nuevo cliente con ID" + clienteID);
        }
    }

    public void eliminarCliente(String clienteID, String proveedorID) {
        clienteRepository.deleteById(clienteID);//si la elimina de acá la elimina de todos lador
    }

    public AdministradorEntity buscarAdministrador(String id, String contrasena) {
        for(AdministradorEntity c: administradorRepository.getListAdministrado()){
            if(Objects.equals(c.getId(), id) && Objects.equals(c.getContrasena(), contrasena)){
                return c;
            }
        }
        return null;
    }

    public void proveedorUpdate(String id, String nombre, String correo, String telefono, String nif, String actividad)throws Exception {
        try{
            ProveedorEntity p = proveedorRepository.findById(id).get();
            p.setNombre(nombre);
            p.setCorreo(correo);
            p.setTelefono(telefono);
            p.setEstado('A');

            HaciendaEntity hE = findHaciendaByNIF(nif);
            hE.setActEconomica(actividad);
            p.setHaciendaByNif(hE);
            proveedorRepository.save(p);
        }catch (Exception e){
            ProveedorEntity p = proveedorRepository.findById(id).get();
            p.setEstado('D');
            proveedorRepository.save(p);
            throw new Exception("Error, el correo digitado ya existe ");
        }
    }
     //Facturacion
     public boolean verificarProducto(String cod, String idProveedor) {
         ProductoEntity producto = productoRepository.findById(cod).orElse(null);
         if (producto.getProveedorByIdProveedor().getId().equals(idProveedor)) {
             return true;
         }
         return false;
     }

    public boolean verificarCliente(String id, String idProveedor) {
        ClienteEntity cliente = clienteRepository.findById(id).orElse(null);
        if (cliente.getProveedorByIdProveedor().getId().equals(idProveedor)) {
            return true;
        }
        return false;
    }

    public List<ProductoEntity> getProductosPorProveedor(String idProveedor) {
        List<ProductoEntity> productosConProveedor = new ArrayList<>();
        for (ProductoEntity producto : productoRepository.findAll()) {
            if (producto.getProveedorByIdProveedor().getId().equals(idProveedor)) {
                productosConProveedor.add(producto);
            }
        }
        return productosConProveedor;
    }

    public List<ClienteEntity> getClientesPorProveedor(String idProveedor) {
        List<ClienteEntity> clientesConProveedor = new ArrayList<>();
        for (ClienteEntity cliente : clienteRepository.findAll()) {
            if (cliente.getProveedorByIdProveedor().getId().equals(idProveedor)) {
                clientesConProveedor.add(cliente);
            }
        }
        return clientesConProveedor;
    }
    public FacturasEntity  returnFactura(String num){return facturaRepository.findById(num).orElse(null);}
    public ClienteEntity returnCliente(String id){return clienteRepository.findById(id).orElse(null);}

    public ProveedorEntity returnProveedor(String id){return proveedorRepository.findById(id).orElse(null);}

    public ProductoEntity returnProducto(String cod){return productoRepository.findById(cod).orElse(null);}


    public void facturaSave(String idProveedor, String idCliente, double total, Collection<LineaServicioEntity> lineas){
        FacturasEntity factura = new FacturasEntity();
        String numeroFactura = String.format("%010d", facturaRepository.count()+1);
        factura.setNumFactura(numeroFactura);
        Date fechaActual = new Date(System.currentTimeMillis());
        Date fecha = new Date(fechaActual.getTime());
        factura.setFechEmision(fecha);
        factura.setTotal(total);
        factura.setClienteByIdCliente(returnCliente(idCliente));
        factura.setProveedorByIdProveedor(returnProveedor(idProveedor));
        facturaRepository.save(factura);
        for (LineaServicioEntity lineaServicio : lineas){
            lineaServicio.setIdLinea(String.valueOf(lineaServicio.getCod()));
            lineaServicio.setCod(lineaServicio.getCod()+Integer.parseInt(lineaServicio.getProductoByCodProducto().getCod()));
            lineaServicio.setFacturasByNumFactura(factura);
            lineaServicioRepository.save(lineaServicio);
        }
    }

    public Iterable<ProductoEntity> productoFindAll(){
        return productoRepository.findAll();
    }

    public Iterable<FacturasEntity> facturaFindAll(){return facturaRepository.findAll();}


}
