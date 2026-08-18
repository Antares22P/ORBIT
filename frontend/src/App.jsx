import { useEffect, useState } from "react";

import { calculateDistance } from "./utils/distance";

import {
  createGroup,
  getGroup,
  addMember,
  getMembers,
  updateMemberLocation,
  getMemberDistance,
  getDestination,
  setDestination,
  deleteDestination,
} from "./services/api";

import "./index.css";

function App() {
  const [screen, setScreen] = useState("home");

  const [groupName, setGroupName] = useState("");
  const [groupId, setGroupId] = useState("");
  const [group, setGroup] = useState(null);

  const [location, setLocation] = useState(null);
  const [locationError, setLocationError] = useState("");
  const [locationLoading, setLocationLoading] = useState(false);

  const [members, setMembers] = useState([]);
  const [memberId, setMemberId] = useState("");

  const [destination, setDestinationState] = useState(null);

  const [destinationLoading, setDestinationLoading] = useState(false);

  // =========================================================
  // LOAD DESTINATION
  // =========================================================

  async function loadDestination() {
    if (!groupId) {
      return;
    }

    try {
      const data = await getDestination(groupId);

      console.log("Destination response:", data);

      setDestinationState(data || null);
    } catch (error) {
      // 404 simply means there is no destination yet.
      if (error.message === "Destination not found") {
        setDestinationState(null);
        return;
      }

      console.error("Failed to load destination:", error);

      setDestinationState(null);
    }
  }
  // =========================================================
  // SET DESTINATION
  // =========================================================

  async function handleSetDestination() {
    if (!location) {
      alert("Please enable your location first.");
      return;
    }

    try {
      setDestinationLoading(true);

      const data = await setDestination(
        groupId,
        location.latitude,
        location.longitude,
        "My Destination",
      );

      console.log("Destination created:", data);

      await loadDestination();
    } catch (error) {
      console.error("Failed to set destination:", error);

      alert(error.message);
    } finally {
      setDestinationLoading(false);
    }
  }

  // =========================================================
  // DELETE DESTINATION
  // =========================================================

  async function handleDeleteDestination() {
    try {
      await deleteDestination(groupId);

      setDestinationState(null);

      console.log("Destination deleted");
    } catch (error) {
      console.error("Failed to delete destination:", error);

      alert(error.message);
    }
  }
  // =========================================================
  // LOAD MEMBERS
  // =========================================================

  async function loadMembers() {
    if (!groupId) {
      return;
    }

    try {
      const data = await getMembers(groupId);

      console.log("Members response:", data);

      if (Array.isArray(data)) {
        setMembers(data);
      } else if (Array.isArray(data?.members)) {
        setMembers(data.members);
      } else if (data && typeof data === "object") {
        setMembers(Object.values(data));
      } else {
        setMembers([]);
      }
    } catch (error) {
      console.error("Failed to load members:", error);
      setMembers([]);
    }
  }

  // =========================================================
  // AUTO REFRESH MEMBERS
  // =========================================================

  useEffect(() => {
    if (screen !== "joined" || !groupId) {
      return;
    }

    loadMembers();
    loadDestination();

    const interval = setInterval(() => {
      loadMembers();
      loadDestination();
    }, 5000);

    return () => {
      clearInterval(interval);
    };
  }, [screen, groupId]);

  // =========================================================
  // START LOCATION
  // =========================================================

  function startLocationTracking() {
    if (!navigator.geolocation) {
      setLocationError("Geolocation is not supported by this browser.");
      return;
    }

    setLocationLoading(true);
    setLocationError("");

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        console.log("RAW GPS POSITION:", position.coords);

        const { latitude, longitude, accuracy, speed, heading } =
          position.coords;

        const newLocation = {
          latitude,
          longitude,
          accuracy,
          speed: speed ?? 0,
          heading: heading ?? 0,
        };

        setLocation(newLocation);
        setLocationLoading(false);

        await sendLocationToBackend(newLocation);
      },

      (error) => {
        console.error("Location error:", error);

        setLocationLoading(false);

        setLocationError(
          "Unable to get your location. Please allow location permission.",
        );
      },

      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 5000,
      },
    );
  }

  // =========================================================
  // SEND LOCATION TO BACKEND
  // =========================================================

  async function sendLocationToBackend(position) {
    if (!groupId) {
      console.error("No group ID available");
      return;
    }

    try {
      let currentMemberId = memberId;

      // -------------------------------------------------------
      // CREATE MEMBER IF NEEDED
      // -------------------------------------------------------

      if (!currentMemberId) {
        const member = await addMember(groupId, {
          name: "You",
          avatar: "user",
          color: "blue",
        });

        console.log("Member created:", member);

        currentMemberId = member.id;

        setMemberId(currentMemberId);
      }

      // -------------------------------------------------------
      // UPDATE LOCATION
      // -------------------------------------------------------

      const result = await updateMemberLocation(
        groupId,
        currentMemberId,
        position.latitude,
        position.longitude,
        position.accuracy,
        position.speed,
        position.heading,
      );

      console.log("Location updated:", result);

      // -------------------------------------------------------
      // REFRESH MEMBERS
      // -------------------------------------------------------

      await loadMembers();
    } catch (error) {
      console.error("Failed to send location:", error);
    }
  }

  // =========================================================
  // CREATE GROUP
  // =========================================================

  async function handleCreateGroup() {
    if (!groupName.trim()) {
      alert("Please enter a group name");
      return;
    }

    try {
      const data = await createGroup(groupName.trim());

      console.log("Create group response:", data);

      const newGroupId = data.id;

      if (!newGroupId) {
        throw new Error("Group ID was not returned by backend");
      }

      setGroupId(newGroupId);
      setGroup(data);

      setMembers([]);
      setMemberId("");
      setLocation(null);
      setLocationError("");

      setScreen("created");
    } catch (error) {
      console.error("Create group failed:", error);

      alert(error.message);
    }
  }

  // =========================================================
  // JOIN GROUP
  // =========================================================

  async function handleJoinGroup() {
    if (!groupId.trim()) {
      alert("Please enter a Group ID");
      return;
    }

    try {
      const data = await getGroup(groupId.trim());

      console.log("Get group response:", data);

      if (!data || !data.id) {
        throw new Error("Group not found");
      }

      setGroupId(data.id);
      setGroup(data);

      setMemberId("");
      setLocation(null);
      setLocationError("");

      try {
        const memberData = await getMembers(data.id);

        console.log("Initial members:", memberData);

        if (Array.isArray(memberData)) {
          setMembers(memberData);
        } else if (Array.isArray(memberData?.members)) {
          setMembers(memberData.members);
        } else if (memberData && typeof memberData === "object") {
          setMembers(Object.values(memberData));
        } else {
          setMembers([]);
        }
      } catch (error) {
        console.error("Failed to load initial members:", error);

        setMembers([]);
      }

      await loadDestination();

      setScreen("joined");
    } catch (error) {
      console.error("Join group failed:", error);

      alert(error.message);
    }
  }

  // =========================================================
  // APP UI
  // =========================================================

  return (
    <div className="app">
      {/* =====================================================
          HOME
      ===================================================== */}

      {screen === "home" && (
        <>
          <h1>ORBIT</h1>

          <p className="subtitle">Live Group Tracker</p>

          <div className="card">
            <h2>Welcome to ORBIT</h2>

            <p>
              Stay connected with your group and track everyone in real time.
            </p>

            <div className="buttons">
              <button onClick={() => setScreen("create")}>Create Group</button>

              <button className="secondary" onClick={() => setScreen("join")}>
                Join Group
              </button>
            </div>
          </div>
        </>
      )}

      {/* =====================================================
          CREATE GROUP
      ===================================================== */}

      {screen === "create" && (
        <>
          <h1>ORBIT</h1>

          <div className="card">
            <h2>Create Group</h2>

            <input
              type="text"
              placeholder="Enter group name"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
            />

            <button onClick={handleCreateGroup}>Create Group</button>

            <button className="back-button" onClick={() => setScreen("home")}>
              Back
            </button>
          </div>
        </>
      )}

      {/* =====================================================
          GROUP CREATED
      ===================================================== */}

      {screen === "created" && (
        <>
          <h1>ORBIT</h1>

          <div className="card">
            <h2>Group Created 🎉</h2>

            <p>Your group ID is:</p>

            <div className="group-id">{groupId}</div>

            <p>Share this ID with your friends so they can join your group.</p>

            <button onClick={() => setScreen("joined")}>Open Group</button>
          </div>
        </>
      )}

      {/* =====================================================
          JOIN GROUP
      ===================================================== */}

      {screen === "join" && (
        <>
          <h1>ORBIT</h1>

          <div className="card">
            <h2>Join Group</h2>

            <input
              type="text"
              placeholder="Enter Group ID"
              value={groupId}
              onChange={(e) => setGroupId(e.target.value)}
            />

            <button onClick={handleJoinGroup}>Join Group</button>

            <button className="back-button" onClick={() => setScreen("home")}>
              Back
            </button>
          </div>
        </>
      )}

      {/* =====================================================
          GROUP DASHBOARD
      ===================================================== */}

      {screen === "joined" && (
        <GroupDashboard
          group={group}
          groupId={groupId}
          members={members}
          location={location}
          destination={destination}
          destinationLoading={destinationLoading}
          onSetDestination={handleSetDestination}
          onDeleteDestination={handleDeleteDestination}
          locationError={locationError}
          locationLoading={locationLoading}
          onStartLocation={startLocationTracking}
          onLeave={() => {
            setGroup(null);
            setGroupId("");
            setMemberId("");
            setLocation(null);
            setLocationError("");
            setMembers([]);
            setScreen("home");
          }}
        />
      )}
    </div>
  );
}

// =============================================================
// GROUP DASHBOARD
// =============================================================

function GroupDashboard({
  group,
  groupId,
  members,
  location,
  destination,
  destinationLoading,
  onSetDestination,
  onDeleteDestination,
  locationError,
  locationLoading,
  onStartLocation,
  onLeave,
}) {
  // =========================================================
  // YOUR LOCATION
  // =========================================================

  const yourLatitude = location?.latitude;

  const yourLongitude = location?.longitude;

  return (
    <div className="dashboard">
      {/* =====================================================
          HEADER
      ===================================================== */}

      <div className="dashboard-header">
        <div>
          <h1>ORBIT</h1>
          <p>Live Group Tracker</p>
        </div>

        <div className="status-dot">●</div>
      </div>

      {/* =====================================================
          GROUP INFO
      ===================================================== */}

      <div className="group-card">
        <h2>{group?.name || "ORBIT Group"}</h2>

        <p className="group-id-small">{groupId}</p>
      </div>

      {/* =====================================================
          MEMBERS
      ===================================================== */}

      <div className="members-card">
        <div className="section-title">
          <h3>Members</h3>

          <span className="member-count">{members.length}</span>
        </div>

        {members.length === 0 ? (
          <p className="empty-members">No members yet.</p>
        ) : (
          members.map((member, index) => {
            // ------------------------------------------------
            // GET COORDINATES
            // ------------------------------------------------

            const latitude = member.latitude ?? member.lat;

            const longitude = member.longitude ?? member.lng;

            // ------------------------------------------------
            // CALCULATE DISTANCE
            // ------------------------------------------------

            let distance = null;

            if (
              yourLatitude !== undefined &&
              yourLongitude !== undefined &&
              latitude !== undefined &&
              longitude !== undefined
            ) {
              distance = calculateDistance(
                Number(yourLatitude),
                Number(yourLongitude),
                Number(latitude),
                Number(longitude),
              );
            }

            // ------------------------------------------------
            // CONNECTION STATUS
            // ------------------------------------------------

            let connectionStatus = "Unknown";

            if (distance !== null) {
              if (distance <= 50) {
                connectionStatus = "Connected";
              } else if (distance <= 75) {
                connectionStatus = "Near";
              } else {
                connectionStatus = "Out of range";
              }
            }

            // ------------------------------------------------
            // STATUS CLASS
            // ------------------------------------------------

            let statusClass = "unknown";

            if (connectionStatus === "Connected") {
              statusClass = "connected";
            } else if (connectionStatus === "Near") {
              statusClass = "near";
            } else if (connectionStatus === "Out of range") {
              statusClass = "disconnected";
            }

            // ------------------------------------------------
            // MEMBER CARD
            // ------------------------------------------------

            return (
              <div className="member" key={member.id || index}>
                <div className="member-icon">
                  {member.name ? member.name.charAt(0).toUpperCase() : "?"}
                </div>

                <div className="member-info">
                  <strong>{member.name || "Unknown"}</strong>

                  <span className={statusClass}>● {connectionStatus}</span>

                  <button onClick={() => onTestMemberDistance(member.id)}>
                    Test Distance
                  </button>

                  {latitude !== undefined && longitude !== undefined && (
                    <small>
                      Location: {Number(latitude).toFixed(5)}
                      {", "}
                      {Number(longitude).toFixed(5)}
                    </small>
                  )}

                  {distance !== null && (
                    <small>
                      Distance:{" "}
                      {distance < 1000
                        ? `${Math.round(distance)} m`
                        : `${(distance / 1000).toFixed(2)} km`}
                    </small>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* =====================================================
    DESTINATION
===================================================== */}

      <div className="destination-card">
        <h3>Destination</h3>

        {!destination ? (
          <>
            <p>📍 No destination set.</p>

            <button
              onClick={onSetDestination}
              disabled={destinationLoading || !location}
            >
              {destinationLoading
                ? "Setting..."
                : "Set Current Location as Destination"}
            </button>

            {!location && <small>Enable your location first.</small>}
          </>
        ) : (
          <>
            <p className="destination-active">🟢 Destination active</p>

            <p>
              <strong>Name:</strong> {destination.name || "Destination"}
            </p>

            <p>
              <strong>Latitude:</strong>{" "}
              {Number(destination.latitude ?? destination.lat).toFixed(6)}
            </p>

            <p>
              <strong>Longitude:</strong>{" "}
              {Number(destination.longitude ?? destination.lng).toFixed(6)}
            </p>

            <button
              className="delete-destination"
              onClick={onDeleteDestination}
            >
              Remove Destination
            </button>
          </>
        )}
      </div>

      {/* =====================================================
          YOUR LOCATION
      ===================================================== */}

      <div className="location-card">
        <h3>Your Location</h3>

        {locationLoading && <p>📡 Getting your location...</p>}

        {!location && !locationLoading && !locationError && (
          <>
            <p>📍 Location not started</p>

            <button onClick={onStartLocation}>Enable Location</button>
          </>
        )}

        {location && (
          <div className="location-data">
            <p>🟢 Location active</p>

            <p>
              <strong>Latitude:</strong> {location.latitude.toFixed(6)}
            </p>

            <p>
              <strong>Longitude:</strong> {location.longitude.toFixed(6)}
            </p>

            <p>
              <strong>Accuracy:</strong> {Math.round(location.accuracy)} m
            </p>

            <button onClick={onStartLocation}>Refresh Location</button>
          </div>
        )}

        {locationError && (
          <div className="location-error">
            <p>⚠️ {locationError}</p>

            <button onClick={onStartLocation}>Try Again</button>
          </div>
        )}
      </div>

      {/* =====================================================
          MAP PLACEHOLDER
      ===================================================== */}

      <div className="map-placeholder">
        <div className="map-placeholder-icon">🗺️</div>

        <h3>Map</h3>

        <p>Map will be added in the next step.</p>
      </div>

      {/* =====================================================
          LEAVE GROUP
      ===================================================== */}

      <button className="leave-button" onClick={onLeave}>
        Leave Group
      </button>
    </div>
  );

  async function testMemberDistance(memberId) {
    if (!groupId || !memberId) {
      console.log("Group ID or Member ID missing");
      return;
    }

    try {
      console.log("Requesting distance for:", memberId);

      const result = await getMemberDistance(groupId, memberId);

      console.log("BACKEND DISTANCE RESPONSE:", result);
    } catch (error) {
      console.error("Distance request failed:", error);
    }
  }
}

export default App;
