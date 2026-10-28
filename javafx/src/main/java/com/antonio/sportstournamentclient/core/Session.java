package com.antonio.sportstournamentclient.core;
import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.user.dto.UserResponse;
/** Holds the current authenticated principal and role permissions. */
public final class Session {
 private static LoginResponse user; private Session(){}
 public static LoginResponse getCurrentUser(){return user;} public static void setCurrentUser(LoginResponse u){user=u;}
 public static void setCurrentUser(UserResponse u){user = new LoginResponse(u.id(), u.name(), u.email(), u.role(), u.twoFactorEnabled());}
 public static LoginResponse currentUser(){return user;} public static void set(LoginResponse u){setCurrentUser(u);}
 public static void clear(){user=null; selectedTeamId=null;}
 private static Long selectedTeamId;
 public static Long getSelectedTeamId(){return selectedTeamId;}
 public static void setSelectedTeamId(Long id){selectedTeamId=id;}
 public static void logout(){clear();} public static boolean isAuthenticated(){return user!=null;}
 public static boolean isAdmin(){return user!=null&&"ADMIN".equals(user.role());}
 public static boolean isOrganizer(){return user!=null&&"ORGANIZER".equals(user.role());}
 public static boolean isTeamManager(){return user!=null&&("ADMIN".equals(user.role())||"TEAM_MANAGER".equals(user.role()));}
 public static boolean admin(){return isAdmin();} public static boolean teamManager(){return isTeamManager();}
}
