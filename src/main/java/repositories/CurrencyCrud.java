package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Currency;

public class CurrencyCrud implements CrudRepository<Currency> {

    @Override
    public Currency findById(Long id) {
        String sqlCommand = """
                            SELECT * FROM public.Currency
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            if (vResultSet.next()) {
                return new Currency(
                        vResultSet.getLong("id"),
                        vResultSet.getString("code"),
                        vResultSet.getString("fullName"),
                        vResultSet.getString("sign").charAt(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> currencyList = new ArrayList<Currency>();
        String sqlCommand = """
                            SELECT * FROM public.Currency
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            while (vResultSet.next()) {
                Currency entry = new Currency(
                        vResultSet.getLong("id"),
                        vResultSet.getString("code"),
                        vResultSet.getString("fullName"),
                        vResultSet.getString("sign").charAt(0));
                currencyList.add(entry);
            }

            return currencyList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Currency entity) {
        String sqlCommand = """
                            INSERT INTO public.Currency (code, fullName, sign)
                            VALUES ('?', '?', '?')
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign().toString());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Currency entity) {
        String sqlCommand = """
                            UPDATE public.Currency
                            SET code = '?', fullName = '?', sign = '?'
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign().toString());
            statement.setLong(4, entity.getId());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        String sqlCommand = """
                            DELETE FROM public.Currency
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
