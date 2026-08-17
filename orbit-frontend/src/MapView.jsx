import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Circle
} from "react-leaflet";

import L from "leaflet";
import "leaflet/dist/leaflet.css";

function createMemberIcon(letter, color = "#00e5ff") {
  return L.divIcon({
    className: "",
    html: `
      <div class="orbit-marker" style="--marker-color:${color}">
        <div class="marker-pulse"></div>
        <div class="marker-avatar">
          ${letter}
        </div>
      </div>
    `,
    iconSize: [50, 50],
    iconAnchor: [25, 25],
  });
}

function MapView() {
  const center = [22.5726, 88.3639];

  return (
    <MapContainer
      center={center}
      zoom={13}
      zoomControl={false}
      className="orbit-map"
    >

      {/* Dark map */}
      <TileLayer
        attribution='&copy; OpenStreetMap &copy; CARTO'
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        maxZoom={20}
      />

      {/* Example members - temporary */}
      <Marker
        position={[22.5726, 88.3639]}
        icon={createMemberIcon("P", "#00e5ff")}
      >
        <Popup>
          <b>You</b>
          <br />
          Live location
        </Popup>
      </Marker>

      <Marker
        position={[22.5780, 88.3680]}
        icon={createMemberIcon("A", "#ff6b9d")}
      >
        <Popup>
          <b>Alex</b>
          <br />
          120 m away
        </Popup>
      </Marker>

      <Marker
        position={[22.5660, 88.3580]}
        icon={createMemberIcon("R", "#ffb703")}
      >
        <Popup>
          <b>Rahul</b>
          <br />
          486 m away
        </Popup>
      </Marker>

    </MapContainer>
  );
}

export default MapView;