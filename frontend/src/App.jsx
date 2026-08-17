import { useState } from "react";
import {
  createGroup,
  getGroup,
  addMember,
  getMembers,
  updateMemberLocation,
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

  async function loadMembers() {
    if (!groupId) {
      return;
    }

    try {
      const data = await getMembers(groupId);

      console.log("Members response:", data);

      if (Array.isArray(data)) {
        setMembers(data);
      } else if (Array.isArray(data.members)) {
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
        const { latitude, longitude, accuracy } = position.coords;

        const newLocation = {
          latitude,
          longitude,
          accuracy,
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

  async function sendLocationToBackend(position) {
    if (!groupId) {
      console.error("No group ID available");
      return;
    }

    try {
      let currentMemberId = memberId;

      // Create member if we don't have one yet
      if (!currentMemberId) {
        const member = await addMember(groupId, {
          name: "You",
        });

        console.log("Member created:", member);

        currentMemberId = member.id;

        setMemberId(currentMemberId);
      }

      // Update GPS location
      const result = await updateMemberLocation(
        groupId,
        currentMemberId,
        position.latitude,
        position.longitude,
        position.accuracy,
        0,
        0,
      );

      console.log("Location updated:", result);
    } catch (error) {
      console.error("Failed to send location:", error);
    }
  }

  async function handleCreateGroup() {
    if (!groupName.trim()) {
      alert("Please enter a group name");
      return;
    }

    try {
      const data = await createGroup(groupName);

      console.log("Create group response:", data);

      const newGroupId = data.id;

      if (!newGroupId) {
        throw new Error("Group ID was not returned by backend");
      }

      setGroupId(newGroupId);
      setGroup(data);
      setScreen("created");
    } catch (error) {
      console.error("Create group failed:", error);
      alert(error.message);
    }
  }

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

      try {
        const memberData = await getMembers(data.id);

        console.log("Initial members:", memberData);

        if (Array.isArray(memberData)) {
          setMembers(memberData);
        } else if (Array.isArray(memberData.members)) {
          setMembers(memberData.members);
        } else if (memberData && typeof memberData === "object") {
          setMembers(Object.values(memberData));
        } else {
          setMembers([]);
        }
      } catch (error) {
        console.error("Failed to load initial members:", error);
      }

      setScreen("joined");
    } catch (error) {
      console.error("Join group failed:", error);
      alert(error.message);
    }
  }

  return (
    <div className="app">
      {/* HOME */}
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

      {/* CREATE GROUP */}
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

      {/* GROUP CREATED */}
      {screen === "created" && (
        <>
          <h1>ORBIT</h1>

          <div className="card">
            <h2>Group Created 🎉</h2>

            <p>Your group ID is:</p>

            <div className="group-id">{groupId}</div>

            <p>Share this ID with your friends so they can join your group.</p>

            <button onClick={() => setScreen("home")}>Done</button>
          </div>
        </>
      )}

      {/* JOIN GROUP */}
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

      {/* JOINED */}
      {screen === "joined" && (
        <GroupDashboard
          group={group}
          groupId={groupId}
          members={members}
          location={location}
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

function GroupDashboard({
  group,
  groupId,
  members,
  location,
  locationError,
  locationLoading,
  onStartLocation,
  onLeave,
}) {
  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1>ORBIT</h1>
          <p>Live Group Tracker</p>
        </div>

        <div className="status-dot">●</div>
      </div>

      <div className="group-card">
        <h2>{group?.name || "ORBIT Group"}</h2>

        <p className="group-id-small">{groupId}</p>
      </div>

      <div className="members-card">
        <div className="section-title">
          <h3>Members</h3>

          <span className="member-count">{members.length}</span>
        </div>

        {members.length === 0 ? (
          <p className="empty-members">No members yet.</p>
        ) : (
          members.map((member, index) => (
  <div
    className="member"
    key={member.id || index}
  >

    <div className="member-icon">
      {member.name
        ? member.name.charAt(0).toUpperCase()
        : "?"}
    </div>

    <div className="member-info">

      <strong>
        {member.name || "Unknown"}
      </strong>

      <span className="connected">
        ● Location available
      </span>

      {member.lat !== undefined &&
        member.lng !== undefined && (
          <small>
            {Number(member.lat).toFixed(5)},
            {" "}
            {Number(member.lng).toFixed(5)}
          </small>
        )}

    </div>

  </div>
))}
      </div>

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

      <button className="map-button">Open Map</button>

      <button className="leave-button" onClick={onLeave}>
        Leave Group
      </button>
    </div>
  );
}

export default App;
