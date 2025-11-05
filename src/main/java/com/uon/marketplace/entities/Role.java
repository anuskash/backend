package com.uon.marketplace.entities;

public enum Role {
    USER,           // Regular users - can manage their own profile, products, reviews
    ADMIN,          // Admins - can ban users, view profiles, reset passwords, verify users
    SUPER_ADMIN     // Super admins - full access including creating admins and deleting users
}
