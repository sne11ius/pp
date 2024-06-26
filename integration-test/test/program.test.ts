import {expect, test} from "@microsoft/tui-test";

test.describe('basic --help tests', () => {
    test.use({program: {file: "../client/pp", args: ['--help']}});

    test("pp shows usage message", async ({terminal}) => {
        await expect(terminal.getByText("pp is a simple TUI planning poker client.", {full: true})).toBeVisible();
    });
});

test.describe('tui tests', () => {
    test.use({program: {file: "../client/pp", args: ['-n', 'nice user', '-s', 'http://localhost:31337']}});
    test("pp can enter random room", async ({terminal}) => {
        await expect(terminal.getByText(/nice user \(\*\)/g)).toBeVisible();
    });
});

test.describe('tui tests', () => {
    test.use({
        program: {
            file: "../client/pp",
            args: ['-n', 'nice user', '-r', 'what a room to be alive', '-s', 'http://localhost:31337']
        }
    });
    test("pp can enter custom room", async ({terminal}) => {
        await expect(terminal.getByText(/nice user \(\*\)/g)).toBeVisible();
        await expect(terminal.getByText(/what a room to be alive/g)).toBeVisible();
    });
});

test.describe('tui tests', () => {

    test("pp shows user name and can quit", async ({terminal}) => {
        terminal.submit('../client/pp -r "not-a-random-room" -n "nice user" -s http://localhost:31337');
        await expect(terminal.getByText(/nice user \(\*\)/g)).toBeVisible();
        const shiftTab = "\u001B[Z";
        terminal.submit(shiftTab) // tab switch back to quit btn
        terminal.submit("\u001B[13~") // enter activate btn for exit
        await expect(terminal.getByText(/version/g)).toBeVisible();
    });

    test("pp can vote", async ({terminal}) => {
        terminal.submit('../client/pp -r "not-a-random-room" -n "nice user" -s http://localhost:31337');
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
        terminal.submit(enter) // reveal button
        await expect(terminal.getByText(/ │☕/g)).toBeVisible();
        terminal.submit(tab)
        terminal.submit(enter) // start new round button
        await expect(terminal.getByText(/││\?/g)).toBeVisible();
        // seems to only work locally
        // terminal.submit(tab)
        // terminal.submit(enter) // copy room name button
        // terminal.submit(tab)
        // terminal.submit(enter)
        // await expect(terminal.getByText(/version/g)).toBeVisible();
        // terminal.submit('xclip -o')
        // await expect(terminal.getByText(/not-a-random-room/g)).toBeVisible();
    });
});
