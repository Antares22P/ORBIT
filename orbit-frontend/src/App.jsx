import { useState } from "react";
import "./App.css";
import MapView from "./MapView";

function App() {
  const [screen, setScreen] = useState("onboarding");

  const [groupName, setGroupName] = useState("");

  function enterOrbit() {
    setScreen("app");
  }

  if (screen === "onboarding") {
    return (
      <div className="onboarding">
        <div className="onboarding-card">

          {/* Logo */}
          <div className="logo-area">
            <div className="logo-orbit">
              <div className="logo-dot"></div>
            </div>

            <div>
              <div className="logo-text">
                ORBIT<span>·</span>
              </div>

              <div className="logo-subtitle">
                LIVE GROUP TRACKING
              </div>
            </div>
          </div>

          {/* Title */}
          <div className="welcome-section">
            <div className="small-label">
              REAL-TIME LOCATION
            </div>

            <h1>
              Stay connected.
              <br />
              <span>Stay together.</span>
            </h1>

            <p>
              Create or join a group and share your
              live location with the people who matter.
            </p>
          </div>

          {/* Group input */}
          <div className="input-section">
            <label>GROUP NAME</label>

            <input
              type="text"
              placeholder="Enter group name"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
            />
          </div>

          {/* Buttons */}
          <button
            className="orbit-button"
            onClick={enterOrbit}
          >
            <span>CREATE GROUP</span>
            <span>→</span>
          </button>

          <button
            className="join-button"
            onClick={enterOrbit}
          >
            JOIN EXISTING GROUP
          </button>

          <div className="security-note">
            <span>●</span>
            LOCATION SHARING IS CONTROLLED BY YOU
          </div>

        </div>
      </div>
    );
  }

  return (
    <div className="orbit-app">

      {/* ================= TOP BAR ================= */}

      <header className="topbar">

        <div className="topbar-left">

          <div className="mini-logo">
            <div></div>
          </div>

          <div>
            <div className="top-logo-text">
              ORBIT<span>·</span>
            </div>

            <div className="group-name">
              {groupName || "MY GROUP"}
            </div>
          </div>

        </div>

        <div className="topbar-right">

          <div className="gps-status">
            <span></span>
            GPS ACTIVE
          </div>

          <button>◐</button>
          <button>🔊</button>

          <button className="members-button">
            👥 <b>3</b>
          </button>

          <button className="leave-button">
            LEAVE
          </button>

        </div>

      </header>


      {/* ================= MAP ================= */}

      <main className="map-container">
  <MapView />
</main>


      {/* ================= MEMBERS PANEL ================= */}

      <aside className="members-panel">

        <div className="panel-header">

          <div>
            <div className="panel-title">
              MEMBERS
            </div>

            <div className="panel-count">
              3 people in this group
            </div>
          </div>

          <button>×</button>

        </div>


        <div className="member-list">

          <Member
            letter="P"
            name="You"
            status="Live"
            distance="0 m"
            current
          />

          <Member
            letter="A"
            name="Alex"
            status="Live"
            distance="124 m"
          />

          <Member
            letter="R"
            name="Rahul"
            status="2 min ago"
            distance="486 m"
            stale
          />

        </div>

      </aside>


      {/* ================= BOTTOM CONTROLS ================= */}

      <div className="bottom-controls">

        <Control icon="⌖" text="Me" />
        <Control icon="◎" text="Follow" />
        <Control icon="⛶" text="Fit All" />
        <Control icon="⌁" text="Mesh" />
        <Control icon="◆" text="Destination" />

        <button className="invite-button">
          ↗ Invite
        </button>

      </div>

    </div>
  );
}


/* ================= MEMBER ================= */

function Member({
  letter,
  name,
  status,
  distance,
  current,
  stale
}) {
  return (
    <div className="member">

      <div className="member-avatar">
        {letter}
      </div>

      <div className="member-info">

        <div className="member-name">
          {name}

          {current && (
            <span className="you-label">
              YOU
            </span>
          )}
        </div>

        <div className="member-status">
          <span
            className={
              stale
                ? "status-dot stale"
                : "status-dot"
            }
          ></span>

          {status}
        </div>

      </div>

      <div className="member-distance">
        {distance}
      </div>

    </div>
  );
}


/* ================= CONTROL ================= */

function Control({ icon, text }) {
  return (
    <button className="control-button">
      <span className="control-icon">
        {icon}
      </span>

      <span>{text}</span>
    </button>
  );
}

export default App;