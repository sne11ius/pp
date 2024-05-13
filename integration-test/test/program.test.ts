import {expect, test} from "@microsoft/tui-test";

test.describe('basic --help tests', () => {
    test.use({program: {file: "../client/pp", args: ['--help']}});

    test("pp shows usage message", async ({terminal}) => {
        await expect(terminal.getByText("pp is a simple TUI planning poker client.", {full: true})).toBeVisible();
    });
});

test.describe('tui tests', () => {
    test.use({program: {file: "../client/pp", args: ['-u', 'nice user', '-s', 'http://localhost:31337', '-r', '']}});
    test("pp can enter random room", async ({terminal}) => {
        await expect(terminal.getByText(/nice user \(\*\)/g)).toBeVisible();
    });
});

test.describe('tui tests', () => {
    test.use({program: {file: "../client/pp", args: ['-u', 'nice user', '-s', 'http://localhost:31337']}});

    test("pp shows user name and can quit", async ({terminal}) => {
        await expect(terminal.getByText(/nice user \(\*\)/g)).toBeVisible();
        const shiftTab = "\u001B[Z";
        terminal.submit(shiftTab) // tab switch back to quit btn
        terminal.submit("\u001B[13~") // enter activate btn for exit
        await expect(terminal.getByText(/version/g)).toBeVisible();
    });

    test("pp can vote", async ({terminal}) => {
        await expect(terminal.getByText(/││\?/g)).toBeVisible();
        const enter = "\u001B[13~";
        terminal.submit(enter) // enter activate btn (focus should be on first vote button by default)
        await expect(terminal.getByText(/││1/g)).toBeVisible();
        const tab = "\u0009";
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││2/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││3/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││5/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││8/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││13/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter)
        await expect(terminal.getByText(/││☕/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter) // submit button
        await expect(terminal.getByText(/ │☕/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter) // start new round button
        await expect(terminal.getByText(/││\?/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(' ')
        await expect(terminal.getByText(/version/g)).toBeVisible();
    });
});
