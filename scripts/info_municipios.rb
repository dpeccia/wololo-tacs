require 'nokogiri'
require 'json'
require 'singleton'
require 'open-uri'

class Adapter
    attr_accessor :api_adapter
    def initialize
        # @api_adapter = api_adapter
    end
    
    def get_municipios(province_name)
        ApiGobierno.instance.get_municipios(province_name)
    end
end

class Municipio
    attr_accessor :altura,:latitud,:longitud
    def initialize(nombre,latitud,longitud,provincia)
        @nombre = nombre
        @latitud = latitud
        @longitud = longitud
        @provincia = provincia
    end
end

module Apis
    def get_json_from_url(url)
        p url
        doc = Nokogiri::HTML(URI.open(url),nil,'UTF-8')
        api_json = JSON.parse(doc)
        return api_json
    end
end

class ApiGobierno
    include Singleton,Apis
    @@municipios_api = "https://apis.datos.gob.ar/georef/api/municipios?provincia=ProvinceName&aplanar=true&campos=estandar&max=10&exacto=true" #TODO cambiar el max que por ahora solo trae 10

    def get_municipios(province_name)
        municipios = []
        coordenadas = []
        api_json = get_json_from_url(@@municipios_api.gsub("ProvinceName",province_name))
        api_json["municipios"].each do |municipio|
            latitud = municipio["centroide_lat"]
            longitud = municipio["centroide_lon"]
            nombre = municipio["nombre"]
            provincia = municipio["provincia_nombre"]
            municipios << Municipio.new(nombre,latitud,longitud,provincia)
            coordenadas << [latitud,longitud]
        end
        p ApiTopoData.instance.get_alturas(municipios)
    end

end

class ApiTopoData
    include Singleton,Apis
    @@topo_data_api = "https://api.opentopodata.org/v1/srtm90m?locations=Coordenadas&interpolation=cubic" #Podes traer varias si lo separas con un |
    def get_alturas(municipios)
        coordenadas_a_buscar = ""
        municipios.each{|municipio| coordenadas_a_buscar << "#{municipio.latitud},#{municipio.longitud}|"}
        api_json = get_json_from_url(@@topo_data_api.gsub("Coordenadas",coordenadas_a_buscar.gsub(/\|$/,"")))
        alturas = api_json["results"].map{|result| result["elevation"]}
        municipios.each_with_index{|municipio, index| municipio.altura = alturas[index]}
    end
end


p Adapter.new.get_municipios("Santa Fe")