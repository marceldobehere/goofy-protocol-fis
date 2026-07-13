'use client';

import Link from "next/link";

export default function Page() {

    return (
        <div style={{ padding: 16, fontFamily: "system-ui, sans-serif" }}>
            <h1 style={{ margin: "0 0 12px" }}>CSS Variables Test</h1>
            <p style={{ margin: "0 0 16px" }}>
                Testing text, disabled states, links, form elements, and separators.
                <br/><br/>
                <Link href={"/"}>Index</Link>
            </p>

            <hr />

            <section style={{ marginTop: 16 }}>
                <h2 style={{ margin: "0 0 8px" }}>Typography & Buttons</h2>

                <p>
                    Normal text.{" "}
                    <button type="button" style={{ marginLeft: 8 }}>
                        Primary Button
                    </button>
                </p>

                <p style={{ marginTop: 12 }}>
                    Disabled button:
                    <button type="button" disabled style={{ marginLeft: 8 }}>
                        Disabled
                    </button>
                </p>
            </section>

            <hr />

            <section style={{ marginTop: 16 }}>
                <h2 style={{ margin: "0 0 8px" }}>Links</h2>

                <p style={{ margin: 0 }}>
                    <a href="#" style={{color: "var(--link-color)"}}>Link</a>
                    <span> &nbsp; &nbsp; </span>
                    <a href="#" style={{color: "var(--link-hover-color)"}}>Link Hover</a>
                    <span> &nbsp; &nbsp; </span>
                    <a href="#" style={{color: "var(--link-active-color)"}}>Link Active</a>
                    <span> &nbsp; &nbsp; </span>
                    <a href="#" style={{color: "var(--link-visited-color)"}}>Link Visited</a>
                </p>
            </section>

            <hr />

            <section style={{ marginTop: 16 }}>
                <h2 style={{ margin: "0 0 8px" }}>Form Controls</h2>

                <div style={{ display: "grid", gap: 10, maxWidth: 520 }}>
                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Text input</span>
                        <input
                            type="text"
                            placeholder="Type here..."
                            defaultValue=""
                        />
                    </label>

                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Disabled text input</span>
                        <input
                            type="text"
                            placeholder="Disabled..."
                            disabled
                            defaultValue=""
                        />
                    </label>

                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Checkbox</span>
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <input type="checkbox" defaultChecked />
                            <span>Accept terms</span>
                        </div>
                    </label>

                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Disabled checkbox</span>
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <input type="checkbox" disabled defaultChecked={false} />
                            <span>Disabled option</span>
                        </div>
                    </label>

                    <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
                        <button type="button">Submit</button>
                        <button type="button" disabled>
                            Submit (disabled)
                        </button>
                    </div>
                </div>
            </section>

            <hr />

            <section style={{ marginTop: 16 }}>
                <h2 style={{ margin: "0 0 8px" }}>Lists</h2>

                <ul style={{ margin: 0, paddingLeft: 18 }}>
                    <li>List item one</li>
                    <li>List item two</li>
                    <li>List item three</li>
                </ul>

                <ol style={{ marginTop: 10, paddingLeft: 18 }}>
                    <li>Ordered one</li>
                    <li>Ordered two</li>
                    <li>Ordered three</li>
                </ol>
            </section>

            <hr />

            <section style={{ marginTop: 16 }}>
                <h2 style={{ margin: "0 0 8px" }}>Secondary Text</h2>
                <p style={{ color: "var(--foreground-disabled)", margin: 0 }}>
                    This paragraph uses a disabled/secondary foreground variable for a visual check.
                </p>
                <p style={{ marginTop: 10 }}>
                    Another line to compare spacing and colors across UI states.
                </p>
            </section>
        </div>
    );
}