-- Create BusTicket Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'BusTicket')
BEGIN
    CREATE DATABASE BusTicket;
END
GO

USE BusTicket;
GO

-- Create basic tables structure (adjust based on your actual schema)

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    CREATE TABLE Users (
        UserId INT PRIMARY KEY IDENTITY(1,1),
        Email NVARCHAR(255) NOT NULL UNIQUE,
        Password NVARCHAR(255) NOT NULL,
        FullName NVARCHAR(255),
        Phone NVARCHAR(20),
        Status INT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),
        UpdatedDate DATETIME DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Routes')
BEGIN
    CREATE TABLE Routes (
        RouteId INT PRIMARY KEY IDENTITY(1,1),
        StartLocation NVARCHAR(255) NOT NULL,
        EndLocation NVARCHAR(255) NOT NULL,
        Distance INT,
        Status INT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),
        UpdatedDate DATETIME DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Buses')
BEGIN
    CREATE TABLE Buses (
        BusId INT PRIMARY KEY IDENTITY(1,1),
        BusNumber NVARCHAR(50) NOT NULL UNIQUE,
        Capacity INT NOT NULL,
        Status INT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),
        UpdatedDate DATETIME DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Trips')
BEGIN
    CREATE TABLE Trips (
        TripId INT PRIMARY KEY IDENTITY(1,1),
        RouteId INT NOT NULL,
        BusId INT NOT NULL,
        DepartureTime DATETIME NOT NULL,
        ArrivalTime DATETIME NOT NULL,
        Price DECIMAL(10,2),
        AvailableSeats INT,
        Status INT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),
        UpdatedDate DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (RouteId) REFERENCES Routes(RouteId),
        FOREIGN KEY (BusId) REFERENCES Buses(BusId)
    );
END
GO

PRINT 'Database BusTicket created successfully!'

